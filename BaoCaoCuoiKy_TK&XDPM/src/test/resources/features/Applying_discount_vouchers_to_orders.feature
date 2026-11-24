Feature: Áp dụng mã giảm giá cho đơn hàng
  In order to tiết kiệm chi phí khi mua hàng
  As a khách hàng có giỏ hàng
  I want to áp dụng mã giảm giá hợp lệ cho đơn hàng

  Background:
    Given khách hàng có một giỏ hàng đang hoạt động

  Rule: Mã giảm giá theo phần trăm hợp lệ được áp dụng và trừ lượt sử dụng
    Example: Áp dụng thành công mã giảm giá 10 phần trăm
      Given đơn hàng có giá trị 1000000 VND
      And mã SALE10 giảm 10 phần trăm
      And mức giảm tối đa là 400000 VND
      And đơn hàng phải đạt tối thiểu 300000 VND
      And mã giảm giá còn 10 lượt sử dụng
      When khách hàng áp dụng mã SALE10
      Then số tiền được giảm là 100000 VND
      And tổng tiền phải thanh toán là 900000 VND
      And số lượt sử dụng còn lại là 9
      And sự kiện VoucherAppliedEvent được phát hành

  Rule: Mã giảm giá theo số tiền cố định được áp dụng khi đơn hàng đủ điều kiện
    Example: Áp dụng thành công mã giảm giá 50000 VND
      Given đơn hàng có giá trị 250000 VND
      And mã FIXED50 giảm cố định 50000 VND
      And giá trị đơn hàng tối thiểu là 200000 VND
      And mã giảm giá còn lượt sử dụng
      When khách hàng áp dụng mã FIXED50
      Then số tiền được giảm là 50000 VND
      And tổng tiền phải thanh toán là 200000 VND
      And số lượt sử dụng giảm đi 1
      And sự kiện VoucherAppliedEvent được phát hành

  Rule: Đơn hàng phải đạt giá trị tối thiểu để sử dụng voucher
    Example: Đơn hàng không đạt giá trị tối thiểu
      Given đơn hàng có giá trị 199999 VND
      And voucher yêu cầu giá trị đơn hàng tối thiểu 200000 VND
      When khách hàng áp dụng voucher
      Then hệ thống từ chối áp dụng voucher
      And tổng tiền đơn hàng không thay đổi
      And số lượt sử dụng voucher không thay đổi
      And sự kiện VoucherAppliedEvent không được phát hành

  Rule: Số tiền giảm giá không được vượt quá giá trị đơn hàng
    Example: Voucher giảm cố định lớn hơn giá trị đơn hàng
      Given đơn hàng có giá trị 80000 VND
      And mã BIGSALE giảm cố định 100000 VND
      And giá trị đơn hàng tối thiểu là 50000 VND
      And voucher còn 5 lượt sử dụng
      When khách hàng áp dụng mã BIGSALE
      Then số tiền được giảm là 80000 VND
      And tổng tiền phải thanh toán là 0 VND
      And số lượt sử dụng voucher giảm đi 1
      And sự kiện VoucherAppliedEvent được phát hành

  Rule: Voucher hết lượt, hết hạn, bị vô hiệu hóa hoặc không tồn tại sẽ bị từ chối
    Scenario Outline: Từ chối voucher không hợp lệ
      Given voucher có trạng thái "<Trạng thái>"
      When khách hàng áp dụng voucher
      Then hệ thống từ chối áp dụng voucher
      And tổng tiền đơn hàng không thay đổi
      And số lượt sử dụng voucher không thay đổi
      And sự kiện VoucherAppliedEvent không được phát hành

      Examples:
        | Trạng thái |
        | EXPIRED    |
        | EXHAUSTED  |
        | DISABLED   |

  Rule: Voucher chỉ được áp dụng cho đơn hàng đang hoạt động
    Scenario Outline: Từ chối áp dụng voucher khi đơn hàng không còn hoạt động
      Given đơn hàng có giá trị 500000 VND
      And đơn hàng có trạng thái "<Trạng thái đơn hàng>"
      And voucher SALE10 giảm 10 phần trăm
      And voucher còn 5 lượt sử dụng
      When khách hàng áp dụng voucher SALE10
      Then hệ thống từ chối áp dụng voucher
      And tổng tiền đơn hàng không thay đổi
      And số lượt sử dụng voucher không thay đổi
      And sự kiện VoucherAppliedEvent không được phát hành

      Examples:
        | Trạng thái đơn hàng |
        | PAID               |
        | CANCELLED          |

  Rule: Voucher phải được xác thực và tính toán giảm giá chính xác
    Scenario Outline: Kiểm tra tính toán và xác thực voucher
      Given đơn hàng có giá trị <Giá trị đơn hàng> VND
      And voucher "<Mã voucher>" có loại "<Loại giảm giá>"
      And voucher có giá trị giảm <Giá trị giảm>
      And mức giảm tối đa là <Mức giảm tối đa>
      And mức chi tiêu tối thiểu là <Chi tiêu tối thiểu>
      And voucher còn <Lượt sử dụng còn lại> lượt sử dụng
      When khách hàng áp dụng voucher
      Then kết quả áp dụng voucher là "<Áp dụng thành công>"
      And số tiền được giảm là <Số tiền được giảm> VND
      And số tiền phải thanh toán là <Số tiền phải trả> VND

      Examples:
        | Giá trị đơn hàng | Mã voucher | Loại giảm giá | Giá trị giảm | Mức giảm tối đa | Chi tiêu tối thiểu | Lượt sử dụng còn lại | Áp dụng thành công | Số tiền được giảm | Số tiền phải trả | Lý do kiểm thử                                     |
        | 1000000          | SALE10     | PERCENTAGE    | 10           | 400000          | 300000             | 5                    | Có                 | 100000            | 900000           | Giảm 10 phần trăm nhưng chạm mức giảm tối đa       |
        | 350000           | SALE10     | PERCENTAGE    | 10           | 40000           | 300000             | 5                    | Có                 | 35000             | 315000           | Giảm 10 phần trăm, chưa chạm mức giảm tối đa       |
        | 200000           | SALE10     | PERCENTAGE    | 10           | 40000           | 300000             | 5                    | Không              | 0                 | 200000           | Không đạt mức chi tiêu tối thiểu                   |
        | 80000            | BIGSALE    | FIXED_AMOUNT  | 100000       | N/A             | 50000              | 5                    | Có                 | 80000             | 0                | Voucher giảm cố định lớn hơn giá trị đơn hàng      |
        | 600000           | FIXED50    | FIXED_AMOUNT  | 50000        | N/A             | 400000             | 0                    | Không              | 0                 | 600000           | Voucher đã hết lượt sử dụng                        |
        | 500000           | FLAT100    | FIXED_AMOUNT  | 100000       | N/A             | 200000             | 2                    | Có                 | 100000            | 400000           | Áp dụng thành công voucher giảm tiền cố định       |
        | 500000           | EXPIRED10  | PERCENTAGE    | 10           | 40000           | 300000             | 5                    | Không              | 0                 | 500000           | Voucher đã hết hạn                                 |

  Rule: Không được áp dụng cùng một voucher nhiều lần cho cùng một đơn hàng
    Example: Khách hàng áp dụng lại voucher đã sử dụng
      Given đơn hàng đã áp dụng voucher SALE10
      When khách hàng tiếp tục áp dụng voucher SALE10
      Then hệ thống từ chối yêu cầu áp dụng voucher
      And không phát sinh thêm sự kiện VoucherAppliedEvent

  Rule: Xử lý thất bại không được làm thay đổi một phần dữ liệu
    Example: Lưu thông tin áp dụng voucher thất bại
      Given đơn hàng đủ điều kiện áp dụng voucher
      And hệ thống gặp lỗi khi lưu dữ liệu
      When khách hàng áp dụng voucher
      Then hệ thống thông báo xử lý thất bại
      And tổng tiền đơn hàng không thay đổi
      And số lượt sử dụng voucher không thay đổi
      And sự kiện VoucherAppliedEvent không được phát hành