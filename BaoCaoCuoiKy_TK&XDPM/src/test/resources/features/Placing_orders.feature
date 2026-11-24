Feature: Đặt hàng
  In order to nhận được các sản phẩm đã chọn
  As a khách hàng có giỏ hàng đang hoạt động
  I want to đặt hàng với thông tin giao hàng, phương thức thanh toán và mã giảm giá (nếu có)

  # QUY ƯỚC CHUNG (dành cho Service, Presenter và Step Definitions)
  
  # Ngữ nghĩa các bước:
  #   - "kèm voucher X": mã gửi lên khi đặt hàng. Nếu khách đã áp dụng X bằng
  #     ApplyVoucherService thì số tiền giảm lấy từ ApplyVoucherOutputDTO,
  #     nếu chưa áp dụng thì số tiền giảm gửi lên là 0.
  #   - Ô trống trong cột "Giảm tối đa" của bảng voucher nghĩa là maxCap = null.
  #   - Mỗi Scenario chỉ vi phạm đúng MỘT rule để dễ truy vết lỗi.
  #   - Phí vận chuyển: 0 khi tạm tính >= 500000, ngược lại 30000.
  #     Tạm tính được xét TRƯỚC khi trừ giảm giá.
  #   - Đặt hàng thành công thì trạng thái đơn chuyển ACTIVE -> PAID.
  #

  Background:
    Given khách hàng "CUS-01" sở hữu giỏ hàng "CART-01" đang hoạt động
    And thông tin giao hàng hợp lệ của người nhận

  
  Rule: Giỏ hàng phải có ít nhất một sản phẩm mới được đặt hàng

    Example: Giỏ hàng rỗng bị từ chối
      Given giỏ hàng không có sản phẩm nào
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then đặt hàng bị từ chối với lý do "Giỏ hàng không có sản phẩm"
      And trạng thái đơn hàng vẫn là "ACTIVE"
      And sự kiện OrderPlacedEvent không được phát hành

  
  Rule: Chỉ chủ giỏ hàng mới được đặt hàng

    Example: Khách hàng khác cố đặt giỏ hàng không phải của mình
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 100000 VND và kho còn 10
      When khách hàng "CUS-02" đặt hàng thanh toán bằng "COD"
      Then đặt hàng bị từ chối với lý do "Chỉ chủ giỏ hàng mới được đặt hàng"
      And trạng thái đơn hàng vẫn là "ACTIVE"
      And tồn kho sản phẩm "P1" là 10
      And sự kiện OrderPlacedEvent không được phát hành

 
  Rule: Chỉ đơn hàng đang hoạt động mới được đặt, đơn đã xử lý không thể đặt lại

    Scenario Outline: Từ chối đặt hàng khi đơn không còn ở trạng thái ACTIVE
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 100000 VND và kho còn 10
      And giỏ hàng có trạng thái "<Trạng thái đơn hàng>"
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then đặt hàng bị từ chối với lý do "Chỉ đơn hàng đang hoạt động mới được đặt hàng"
      And trạng thái đơn hàng vẫn là "<Trạng thái đơn hàng>"
      And tồn kho sản phẩm "P1" là 10
      And sự kiện OrderPlacedEvent không được phát hành

      Examples:
        | Trạng thái đơn hàng |
        | PAID                |
        | CANCELLED           |
        | SHIPPING            |
        | DELIVERED           |

    Example: Bấm đặt hàng hai lần liên tiếp chỉ có hiệu lực một lần
      Given giỏ hàng có sản phẩm "P1" số lượng 2 đơn giá 100000 VND và kho còn 10
      And khách hàng đã đặt hàng thành công một lần bằng "COD"
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then đặt hàng bị từ chối với lý do "Chỉ đơn hàng đang hoạt động mới được đặt hàng"
      And tồn kho sản phẩm "P1" là 8
      And chỉ có đúng 1 sự kiện OrderPlacedEvent được phát hành

  
  Rule: Số lượng mỗi sản phẩm phải là số nguyên dương và không vượt quá tồn kho

    Scenario Outline: Kiểm tra số lượng so với tồn kho
      Given giỏ hàng có sản phẩm "P1" số lượng <Số lượng> đơn giá 100000 VND và kho còn <Tồn kho>
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then kết quả đặt hàng là "<Kết quả>"
      And thông báo đặt hàng là "<Thông báo>"
      And tồn kho sản phẩm "P1" là <Tồn kho sau>

      Examples:
        | Số lượng | Tồn kho | Kết quả | Tồn kho sau | Thông báo                                              | Lý do kiểm thử                        |
        | 1        | 10      | Có      | 9           | Đặt hàng thành công                                    | Số lượng nhỏ nhất hợp lệ              |
        | 10       | 10      | Có      | 0           | Đặt hàng thành công                                    | Biên: mua đúng bằng tồn kho           |
        | 11       | 10      | Không   | 10          | Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho   | Biên: vượt tồn kho 1 đơn vị           |
        | 0        | 10      | Không   | 10          | Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho   | Số lượng bằng 0                       |
        | -1       | 10      | Không   | 10          | Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho   | Số lượng âm                           |
        | 5        | 0       | Không   | 0           | Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho   | Sản phẩm đã hết hàng                  |

    Example: Một dòng vượt tồn kho thì toàn bộ đơn bị từ chối, không trừ kho dòng còn lại
      Given giỏ hàng có các sản phẩm sau:
        | Mã sản phẩm | Số lượng | Đơn giá | Tồn kho |
        | P1          | 2        | 150000  | 10      |
        | P2          | 5        | 250000  | 3       |
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then đặt hàng bị từ chối với lý do "Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho"
      And tồn kho sản phẩm "P1" là 10
      And tồn kho sản phẩm "P2" là 3
      And sự kiện OrderPlacedEvent không được phát hành

  
  Rule: Thông tin giao hàng phải đầy đủ và hợp lệ

    Scenario Outline: Kiểm tra thông tin giao hàng
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 100000 VND và kho còn 10
      And thông tin giao hàng gồm người nhận "<Tên người nhận>", số điện thoại "<Số điện thoại>", địa chỉ "<Địa chỉ>"
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then kết quả đặt hàng là "<Kết quả>"
      And thông báo đặt hàng là "<Thông báo>"

      Examples:
        | Tên người nhận | Số điện thoại | Địa chỉ               | Kết quả | Thông báo                         | Lý do kiểm thử                    |
        | Nguyễn Văn A   | 0912345678    | 12 Nguyễn Huệ, Quận 1 | Có      | Đặt hàng thành công               | Thông tin hợp lệ                  |
        |                | 0912345678    | 12 Nguyễn Huệ, Quận 1 | Không   | Thông tin giao hàng không hợp lệ  | Thiếu tên người nhận              |
        | Nguyễn Văn A   | 091234567     | 12 Nguyễn Huệ, Quận 1 | Không   | Thông tin giao hàng không hợp lệ  | SĐT 9 số (thiếu 1 số)             |
        | Nguyễn Văn A   | 09123456789   | 12 Nguyễn Huệ, Quận 1 | Không   | Thông tin giao hàng không hợp lệ  | SĐT 11 số (dư 1 số)               |
        | Nguyễn Văn A   | 1912345678    | 12 Nguyễn Huệ, Quận 1 | Không   | Thông tin giao hàng không hợp lệ  | SĐT không bắt đầu bằng 0          |
        | Nguyễn Văn A   | 09123abc78    | 12 Nguyễn Huệ, Quận 1 | Không   | Thông tin giao hàng không hợp lệ  | SĐT chứa ký tự không phải số      |
        | Nguyễn Văn A   | 0912345678    |                       | Không   | Thông tin giao hàng không hợp lệ  | Thiếu địa chỉ                     |

  
  Rule: Thanh toán COD chỉ áp dụng cho đơn có tổng phải trả tối đa 20000000 VND

    Scenario Outline: Giới hạn giá trị đơn hàng theo phương thức thanh toán
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá <Đơn giá> VND và kho còn 5
      When khách hàng đặt hàng thanh toán bằng "<Phương thức>"
      Then kết quả đặt hàng là "<Kết quả>"
      And thông báo đặt hàng là "<Thông báo>"

      Examples:
        | Đơn giá  | Phương thức   | Kết quả | Thông báo                                               | Lý do kiểm thử                         |
        | 20000000 | COD           | Có      | Đặt hàng thành công                                     | Biên: đúng bằng mức tối đa của COD     |
        | 20000001 | COD           | Không   | Thanh toán COD chỉ áp dụng cho đơn tối đa 20000000 VND  | Biên: vượt mức tối đa 1 VND            |
        | 25000000 | BANK_TRANSFER | Có      | Đặt hàng thành công                                     | Chuyển khoản không bị giới hạn         |
        | 25000000 | E_WALLET      | Có      | Đặt hàng thành công                                     | Ví điện tử không bị giới hạn           |

    Scenario Outline: Giới hạn COD được xét trên tổng phải trả sau khi giảm giá
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 20500000 VND và kho còn 5
      And voucher khả dụng như sau:
        | Mã      | Loại         | Giá trị       | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | FIXBIG  | FIXED_AMOUNT | <Giá trị giảm> |             | 100000             | 3            |
      And khách hàng đã áp dụng voucher "FIXBIG" cho giỏ hàng
      When khách hàng đặt hàng thanh toán bằng "COD" kèm voucher "FIXBIG"
      Then kết quả đặt hàng là "<Kết quả>"
      And thông báo đặt hàng là "<Thông báo>"

      Examples:
        | Giá trị giảm | Kết quả | Thông báo                                               | Lý do kiểm thử                                |
        | 600000       | Có      | Đặt hàng thành công                                     | Tổng sau giảm 19900000 nên COD hợp lệ         |
        | 400000       | Không   | Thanh toán COD chỉ áp dụng cho đơn tối đa 20000000 VND  | Tổng sau giảm 20100000 vẫn vượt mức COD       |

  
  Rule: Tổng thanh toán bằng tạm tính trừ giảm giá cộng phí vận chuyển, miễn phí vận chuyển khi tạm tính từ 500000 VND

    Scenario Outline: Tính tiền đơn hàng không dùng voucher
      Given giỏ hàng có sản phẩm "P1" số lượng <Số lượng> đơn giá <Đơn giá> VND và kho còn 100
      When khách hàng đặt hàng thanh toán bằng "BANK_TRANSFER"
      Then đặt hàng thành công
      And tạm tính là <Tạm tính> VND
      And số tiền giảm giá là 0 VND
      And phí vận chuyển là <Phí vận chuyển> VND
      And tổng thanh toán là <Tổng thanh toán> VND

      Examples:
        | Số lượng | Đơn giá | Tạm tính | Phí vận chuyển | Tổng thanh toán | Lý do kiểm thử                                 |
        | 1        | 400000  | 400000   | 30000          | 430000          | Dưới ngưỡng freeship nên tính phí              |
        | 1        | 499999  | 499999   | 30000          | 529999          | Biên: thiếu 1 VND so với ngưỡng freeship       |
        | 1        | 500000  | 500000   | 0              | 500000          | Biên: đúng ngưỡng freeship                     |
        | 3        | 200000  | 600000   | 0              | 600000          | Trên ngưỡng freeship                           |

    Example: Tạm tính là tổng thành tiền của tất cả các dòng sản phẩm
      Given giỏ hàng có các sản phẩm sau:
        | Mã sản phẩm | Số lượng | Đơn giá | Tồn kho |
        | P1          | 2        | 150000  | 10      |
        | P2          | 1        | 99999   | 10      |
      When khách hàng đặt hàng thanh toán bằng "BANK_TRANSFER"
      Then đặt hàng thành công
      And tạm tính là 399999 VND
      And phí vận chuyển là 30000 VND
      And tổng thanh toán là 429999 VND

    Scenario Outline: Freeship được xét trên tạm tính trước khi trừ giảm giá
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá <Đơn giá> VND và kho còn 100
      And voucher khả dụng như sau:
        | Mã     | Loại   | Giá trị   | Giảm tối đa   | Chi tiêu tối thiểu | Lượt còn lại |
        | VC-OR7 | <Loại> | <Giá trị> | <Giảm tối đa> | 100000             | 3            |
      And khách hàng đã áp dụng voucher "VC-OR7" cho giỏ hàng
      When khách hàng đặt hàng thanh toán bằng "BANK_TRANSFER" kèm voucher "VC-OR7"
      Then đặt hàng thành công
      And tạm tính là <Tạm tính> VND
      And số tiền giảm giá là <Số tiền giảm> VND
      And phí vận chuyển là <Phí vận chuyển> VND
      And tổng thanh toán là <Tổng thanh toán> VND

      Examples:
        | Đơn giá | Loại         | Giá trị | Giảm tối đa | Tạm tính | Số tiền giảm | Phí vận chuyển | Tổng thanh toán | Lý do kiểm thử                                                |
        | 600000  | PERCENTAGE   | 10      | 40000       | 600000   | 40000        | 0              | 560000          | Giảm phần trăm chạm mức giảm tối đa, vẫn freeship             |
        | 520000  | FIXED_AMOUNT | 50000   |             | 520000   | 50000        | 0              | 470000          | Sau giảm còn 470000 nhưng tạm tính 520000 nên vẫn freeship    |
        | 400000  | PERCENTAGE   | 10      | 40000       | 400000   | 40000        | 30000          | 390000          | Tạm tính dưới ngưỡng nên vẫn tính phí vận chuyển              |

  
  Rule: Voucher (nếu có) phải đã được áp dụng cho đúng đơn hàng, số tiền giảm hợp lệ và không bị trừ lượt lần thứ hai

    Example: Đặt hàng với voucher đã áp dụng và không trừ thêm lượt sử dụng
      Given giỏ hàng có sản phẩm "P1" số lượng 2 đơn giá 300000 VND và kho còn 10
      And voucher khả dụng như sau:
        | Mã     | Loại       | Giá trị | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | SALE10 | PERCENTAGE | 10      | 100000      | 300000             | 5            |
      And khách hàng đã áp dụng voucher "SALE10" cho giỏ hàng
      When khách hàng đặt hàng thanh toán bằng "BANK_TRANSFER" kèm voucher "SALE10"
      Then đặt hàng thành công
      And tạm tính là 600000 VND
      And số tiền giảm giá là 60000 VND
      And phí vận chuyển là 0 VND
      And tổng thanh toán là 540000 VND
      And lượt sử dụng của voucher "SALE10" là 4

    Example: Đặt hàng không dùng voucher vẫn thành công
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 600000 VND và kho còn 10
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then đặt hàng thành công
      And số tiền giảm giá là 0 VND
      And tổng thanh toán là 600000 VND

    Example: Khách khai báo voucher nhưng chưa áp dụng cho giỏ hàng
      Given giỏ hàng có sản phẩm "P1" số lượng 2 đơn giá 300000 VND và kho còn 10
      And voucher khả dụng như sau:
        | Mã     | Loại       | Giá trị | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | SALE10 | PERCENTAGE | 10      | 100000      | 300000             | 5            |
      When khách hàng đặt hàng thanh toán bằng "COD" kèm voucher "SALE10"
      Then đặt hàng bị từ chối với lý do "Voucher chưa được áp dụng cho đơn hàng này"
      And trạng thái đơn hàng vẫn là "ACTIVE"
      And lượt sử dụng của voucher "SALE10" là 5
      And sự kiện OrderPlacedEvent không được phát hành

    Example: Voucher đã áp dụng cho giỏ hàng khác không được dùng cho giỏ hàng này
      Given giỏ hàng có sản phẩm "P1" số lượng 2 đơn giá 300000 VND và kho còn 10
      And voucher khả dụng như sau:
        | Mã     | Loại       | Giá trị | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | SALE10 | PERCENTAGE | 10      | 100000      | 300000             | 5            |
      And khách hàng đã áp dụng voucher "SALE10" cho giỏ hàng "CART-02"
      When khách hàng đặt hàng thanh toán bằng "COD" kèm voucher "SALE10"
      Then đặt hàng bị từ chối với lý do "Voucher chưa được áp dụng cho đơn hàng này"
      And lượt sử dụng của voucher "SALE10" là 4
      And sự kiện OrderPlacedEvent không được phát hành

    Scenario Outline: Từ chối khi số tiền giảm giá gửi lên không hợp lệ
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 300000 VND và kho còn 10
      And voucher khả dụng như sau:
        | Mã     | Loại       | Giá trị | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | SALE10 | PERCENTAGE | 10      | 100000      | 100000             | 5            |
      And khách hàng đã áp dụng voucher "SALE10" cho giỏ hàng
      When khách hàng đặt hàng thanh toán bằng "COD" kèm voucher "SALE10" với số tiền giảm khai báo <Số tiền giảm khai báo> VND
      Then đặt hàng bị từ chối với lý do "Số tiền giảm giá không hợp lệ"
      And sự kiện OrderPlacedEvent không được phát hành

      Examples:
        | Số tiền giảm khai báo | Lý do kiểm thử                              |
        | -1                    | Số tiền giảm âm                             |
        | 300001                | Biên: giảm vượt tạm tính 1 VND              |

    Example: Voucher giảm đúng bằng tạm tính, tổng chỉ còn phí vận chuyển
      Given giỏ hàng có sản phẩm "P1" số lượng 1 đơn giá 80000 VND và kho còn 10
      And voucher khả dụng như sau:
        | Mã      | Loại         | Giá trị | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | BIGSALE | FIXED_AMOUNT | 100000  |             | 50000              | 5            |
      And khách hàng đã áp dụng voucher "BIGSALE" cho giỏ hàng
      When khách hàng đặt hàng thanh toán bằng "COD" kèm voucher "BIGSALE"
      Then đặt hàng thành công
      And tạm tính là 80000 VND
      And số tiền giảm giá là 80000 VND
      And phí vận chuyển là 30000 VND
      And tổng thanh toán là 30000 VND
      And lượt sử dụng của voucher "BIGSALE" là 4

  
  Rule: Đặt hàng thành công thì lưu đơn, chuyển sang PAID, trừ tồn kho và phát hành OrderPlacedEvent

    Example: Đặt hàng thành công nhiều sản phẩm
      Given giỏ hàng có các sản phẩm sau:
        | Mã sản phẩm | Số lượng | Đơn giá | Tồn kho |
        | P1          | 2        | 150000  | 10      |
        | P2          | 1        | 250000  | 3       |
      When khách hàng đặt hàng thanh toán bằng "BANK_TRANSFER"
      Then đặt hàng thành công
      And thông báo đặt hàng là "Đặt hàng thành công"
      And tổng thanh toán là 550000 VND
      And đơn hàng "CART-01" được lưu với trạng thái "PAID"
      And tồn kho sản phẩm "P1" là 8
      And tồn kho sản phẩm "P2" là 2
      And sự kiện OrderPlacedEvent được phát hành
      And sự kiện OrderPlacedEvent chứa mã đơn "CART-01" và tổng thanh toán 550000 VND

 
  Rule: Lỗi khi lưu đơn hàng không được làm thay đổi một phần dữ liệu

    Example: Lưu đơn hàng thất bại thì giữ nguyên trạng thái, tồn kho và không phát sự kiện
      Given giỏ hàng có các sản phẩm sau:
        | Mã sản phẩm | Số lượng | Đơn giá | Tồn kho |
        | P1          | 2        | 150000  | 10      |
        | P2          | 1        | 250000  | 3       |
      And hệ thống gặp lỗi khi lưu đơn hàng
      When khách hàng đặt hàng thanh toán bằng "COD"
      Then đặt hàng bị từ chối với lý do "Lỗi hệ thống khi lưu đơn hàng"
      And trạng thái đơn hàng vẫn là "ACTIVE"
      And tồn kho sản phẩm "P1" là 10
      And tồn kho sản phẩm "P2" là 3
      And sự kiện OrderPlacedEvent không được phát hành

    Example: Lưu đơn hàng thất bại không trừ thêm lượt voucher ở bước đặt hàng
      Given giỏ hàng có sản phẩm "P1" số lượng 2 đơn giá 300000 VND và kho còn 10
      And voucher khả dụng như sau:
        | Mã     | Loại       | Giá trị | Giảm tối đa | Chi tiêu tối thiểu | Lượt còn lại |
        | SALE10 | PERCENTAGE | 10      | 100000      | 300000             | 5            |
      And khách hàng đã áp dụng voucher "SALE10" cho giỏ hàng
      And hệ thống gặp lỗi khi lưu đơn hàng
      When khách hàng đặt hàng thanh toán bằng "COD" kèm voucher "SALE10"
      Then đặt hàng bị từ chối với lý do "Lỗi hệ thống khi lưu đơn hàng"
      And tồn kho sản phẩm "P1" là 10
      And lượt sử dụng của voucher "SALE10" là 4
      And sự kiện OrderPlacedEvent không được phát hành
      # Ghi chú: lượt 4 là kết quả của bước ÁP DỤNG voucher trước đó (5 -> 4).
      # Hoàn lại lượt khi đặt hàng thất bại là hạn chế đã biết của thiết kế hiện tại
      # (ApplyVoucherService không có thao tác hoàn lượt), cần ghi trong báo cáo.
