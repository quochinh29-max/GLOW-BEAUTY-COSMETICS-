package starter.stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import starter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaceOrderStepDefinitions {

    private InMemoryOrderDBMock orderDb;
    private InMemoryVoucherDBMock voucherDb;
    private SimpleOrderPresenter presenter;
    private InMemoryEventPublisherMock eventPublisher;
    private PlaceOrderService placeOrderService;
    private ApplyVoucherService applyVoucherService;

    private String currentCustomerId = "CUS-01";
    private String currentOrderId = "CART-01";
    private List<OrderLine> currentItems = new ArrayList<>();
    private ShippingInfo currentShippingInfo;
    private PaymentMethod currentPaymentMethod = PaymentMethod.COD;
    private String currentVoucherCode = null;
    private double currentDiscountAmount = 0.0;
    private PlaceOrderOutputDTO lastOutput;

    @Before
    public void setUp() {
        orderDb = new InMemoryOrderDBMock();
        voucherDb = new InMemoryVoucherDBMock();
        presenter = new SimpleOrderPresenter();
        eventPublisher = new InMemoryEventPublisherMock();
        placeOrderService = new PlaceOrderService(orderDb, voucherDb, presenter, eventPublisher);
        applyVoucherService = new ApplyVoucherService(voucherDb, new SimpleVoucherPresenter(), eventPublisher);

        currentCustomerId = "CUS-01";
        currentOrderId = "CART-01";
        currentItems = new ArrayList<>();
        currentShippingInfo = new ShippingInfo("Nguyễn Văn A", "0912345678", "12 Nguyễn Huệ, Quận 1");
        currentPaymentMethod = PaymentMethod.COD;
        currentVoucherCode = null;
        currentDiscountAmount = 0.0;
        lastOutput = null;

        orderDb.addOrder(currentOrderId, currentCustomerId, OrderStatus.ACTIVE);
    }

    @Given("khách hàng {string} sở hữu giỏ hàng {string} đang hoạt động")
    public void khachHangSoHuuGioHangDangHoatDong(String customerId, String orderId) {
        this.currentCustomerId = customerId;
        this.currentOrderId = orderId;
        orderDb.addOrder(orderId, customerId, OrderStatus.ACTIVE);
    }

    @Given("thông tin giao hàng hợp lệ của người nhận")
    public void thongTinGiaoHangHopLe() {
        this.currentShippingInfo = new ShippingInfo("Nguyễn Văn A", "0912345678", "12 Nguyễn Huệ, Quận 1");
    }

    @Given("giỏ hàng không có sản phẩm nào")
    public void gioHangKhongCoSanPham() {
        this.currentItems.clear();
    }

    @Given("giỏ hàng có sản phẩm {string} số lượng {int} đơn giá {double} VND và kho còn {int}")
    public void gioHangCoSanPham(String productId, int quantity, double unitPrice, int stock) {
        currentItems.add(new OrderLine(productId, quantity, unitPrice));
        orderDb.addStock(productId, stock);
    }

    @Given("giỏ hàng có trạng thái {string}")
    public void gioHangCoTrangThai(String status) {
        OrderStatus os = OrderStatus.valueOf(status.trim().toUpperCase());
        orderDb.updateOrderStatus(currentOrderId, os);
    }

    @Given("khách hàng đã đặt hàng thành công một lần bằng {string}")
    public void khachHangDaDatHangThanhCong(String method) {
        this.currentPaymentMethod = PaymentMethod.valueOf(method.trim().toUpperCase());
        PlaceOrderModelDTO input = new PlaceOrderModelDTO(currentOrderId, currentCustomerId,
                currentItems, currentShippingInfo, currentPaymentMethod, null, 0.0);
        lastOutput = placeOrderService.execute(input);
        assertThat(lastOutput.isSuccess()).isTrue();
    }

    @Given("giỏ hàng có các sản phẩm sau:")
    public void gioHangCoCacSanPhamSau(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String pId = row.get("Mã sản phẩm");
            int qty = Integer.parseInt(row.get("Số lượng"));
            double price = Double.parseDouble(row.get("Đơn giá"));
            int stock = Integer.parseInt(row.get("Tồn kho"));
            currentItems.add(new OrderLine(pId, qty, price));
            orderDb.addStock(pId, stock);
        }
    }

    @Given("thông tin giao hàng gồm người nhận {string}, số điện thoại {string}, địa chỉ {string}")
    public void thongTinGiaoHangGom(String name, String phone, String addr) {
        this.currentShippingInfo = new ShippingInfo(name, phone, addr);
    }

    @Given("voucher khả dụng như sau:")
    public void voucherKhaDungNhuSau(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            String code = row.get("Mã");
            DiscountType type = DiscountType.valueOf(row.get("Loại").trim().toUpperCase());
            double val = Double.parseDouble(row.get("Giá trị"));
            String maxCapStr = row.get("Giảm tối đa");
            Double maxCap = (maxCapStr != null && !maxCapStr.trim().isEmpty() && !maxCapStr.trim().equalsIgnoreCase("N/A"))
                    ? Double.parseDouble(maxCapStr.trim()) : null;
            double minSpend = Double.parseDouble(row.get("Chi tiêu tối thiểu"));
            int remaining = Integer.parseInt(row.get("Lượt còn lại"));

            Voucher v = new Voucher(code, type, val, maxCap, minSpend, remaining, VoucherStatus.ACTIVE);
            voucherDb.addVoucher(v);
        }
    }

    @Given("khách hàng đã áp dụng voucher {string} cho giỏ hàng")
    public void khachHangDaApDungVoucherChoGioHang(String code) {
        double subtotal = 0.0;
        for (OrderLine line : currentItems) {
            subtotal += line.lineTotal();
        }
        ApplyVoucherModelDTO applyInput = new ApplyVoucherModelDTO(currentOrderId, subtotal, code);
        ApplyVoucherOutputDTO applyOut = applyVoucherService.execute(applyInput);
        this.currentVoucherCode = code;
        this.currentDiscountAmount = applyOut.isSuccess() ? applyOut.getDiscountAmount() : 0.0;
    }

    @Given("khách hàng đã áp dụng voucher {string} cho giỏ hàng {string}")
    public void khachHangDaApDungVoucherChoGioHangKhac(String code, String otherOrderId) {
        double subtotal = 0.0;
        for (OrderLine line : currentItems) {
            subtotal += line.lineTotal();
        }
        ApplyVoucherModelDTO applyInput = new ApplyVoucherModelDTO(otherOrderId, subtotal, code);
        applyVoucherService.execute(applyInput);
    }

    @Given("hệ thống gặp lỗi khi lưu đơn hàng")
    public void heThongGapLoiKhiLuuDonHang() {
        orderDb.setSimulateError(true);
    }

    @When("khách hàng đặt hàng thanh toán bằng {string}")
    public void khachHangDatHangThanhToanBang(String method) {
        this.currentPaymentMethod = PaymentMethod.valueOf(method.trim().toUpperCase());
        PlaceOrderModelDTO input = new PlaceOrderModelDTO(currentOrderId, currentCustomerId,
                currentItems, currentShippingInfo, currentPaymentMethod, currentVoucherCode, currentDiscountAmount);
        lastOutput = placeOrderService.execute(input);
    }

    @When("khách hàng {string} đặt hàng thanh toán bằng {string}")
    public void khachHangKhacDatHang(String otherCustomer, String method) {
        this.currentPaymentMethod = PaymentMethod.valueOf(method.trim().toUpperCase());
        PlaceOrderModelDTO input = new PlaceOrderModelDTO(currentOrderId, otherCustomer,
                currentItems, currentShippingInfo, currentPaymentMethod, currentVoucherCode, currentDiscountAmount);
        lastOutput = placeOrderService.execute(input);
    }

    @When("khách hàng đặt hàng thanh toán bằng {string} kèm voucher {string}")
    public void khachHangDatHangKemVoucher(String method, String code) {
        this.currentPaymentMethod = PaymentMethod.valueOf(method.trim().toUpperCase());
        this.currentVoucherCode = code;
        PlaceOrderModelDTO input = new PlaceOrderModelDTO(currentOrderId, currentCustomerId,
                currentItems, currentShippingInfo, currentPaymentMethod, currentVoucherCode, currentDiscountAmount);
        lastOutput = placeOrderService.execute(input);
    }

    @When("khách hàng đặt hàng thanh toán bằng {string} kèm voucher {string} với số tiền giảm khai báo {double} VND")
    public void khachHangDatHangKemVoucherKhaiBaoGiam(String method, String code, double discountDeclared) {
        this.currentPaymentMethod = PaymentMethod.valueOf(method.trim().toUpperCase());
        this.currentVoucherCode = code;
        this.currentDiscountAmount = discountDeclared;
        PlaceOrderModelDTO input = new PlaceOrderModelDTO(currentOrderId, currentCustomerId,
                currentItems, currentShippingInfo, currentPaymentMethod, currentVoucherCode, currentDiscountAmount);
        lastOutput = placeOrderService.execute(input);
    }

    @Then("đặt hàng bị từ chối với lý do {string}")
    public void datHangBiTuChoiVoiLyDo(String reason) {
        assertThat(lastOutput.isSuccess()).isFalse();
        assertThat(lastOutput.getMessage()).isEqualTo(reason);
    }

    @Then("đặt hàng thành công")
    public void datHangThanhCong() {
        assertThat(lastOutput.isSuccess()).isTrue();
    }

    @Then("kết quả đặt hàng là {string}")
    public void ketQuaDatHangLa(String result) {
        boolean expected = result.equalsIgnoreCase("Có");
        assertThat(lastOutput.isSuccess()).isEqualTo(expected);
    }

    @Then("thông báo đặt hàng là {string}")
    public void thongBaoDatHangLa(String message) {
        assertThat(lastOutput.getMessage()).isEqualTo(message);
    }

    @Then("trạng thái đơn hàng vẫn là {string}")
    public void trangThaiDonHangVanLa(String status) {
        OrderStatus expected = OrderStatus.valueOf(status.trim().toUpperCase());
        assertThat(orderDb.getOrderStatus(currentOrderId)).isEqualTo(expected);
    }

    @Then("tồn kho sản phẩm {string} là {int}")
    public void tonKhoSanPhamLa(String productId, int expectedStock) {
        assertThat(orderDb.getStock(productId)).isEqualTo(expectedStock);
    }

    @Then("tạm tính là {double} VND")
    public void tamTinhLa(double subtotal) {
        assertThat(lastOutput.getSubtotal()).isEqualTo(subtotal);
    }

    @Then("số tiền giảm giá là {double} VND")
    public void soTienGiamGiaLa(double discount) {
        assertThat(lastOutput.getDiscountAmount()).isEqualTo(discount);
    }

    @Then("phí vận chuyển là {double} VND")
    public void phiVanChuyenLa(double shippingFee) {
        assertThat(lastOutput.getShippingFee()).isEqualTo(shippingFee);
    }

    @Then("tổng thanh toán là {double} VND")
    public void tongThanhToanLa(double total) {
        assertThat(lastOutput.getTotalPayable()).isEqualTo(total);
    }

    @Then("lượt sử dụng của voucher {string} là {int}")
    public void luotSuDungVoucherLa(String code, int remaining) {
        Voucher v = voucherDb.findVoucher(code);
        assertThat(v).isNotNull();
        assertThat(v.getRemainingUses()).isEqualTo(remaining);
    }

    @Then("đơn hàng {string} được lưu với trạng thái {string}")
    public void donHangDuocLuuVoiTrangThai(String orderId, String status) {
        OrderStatus expected = OrderStatus.valueOf(status.trim().toUpperCase());
        assertThat(orderDb.getOrderStatus(orderId)).isEqualTo(expected);
    }

    @Then("sự kiện OrderPlacedEvent được phát hành")
    public void suKienOrderPlacedEventDuocPhatHanh() {
        assertThat(eventPublisher.hasPublished(OrderPlacedEvent.class)).isTrue();
    }

    @Then("sự kiện OrderPlacedEvent không được phát hành")
    public void suKienOrderPlacedEventKhongDuocPhatHanh() {
        assertThat(eventPublisher.hasPublished(OrderPlacedEvent.class)).isFalse();
    }

    @Then("chỉ có đúng {int} sự kiện OrderPlacedEvent được phát hành")
    public void chiCoDungSuKienPhatHanh(int count) {
        long actualCount = eventPublisher.getEvents().stream()
                .filter(e -> e instanceof OrderPlacedEvent)
                .count();
        assertThat(actualCount).isEqualTo(count);
    }

    @Then("sự kiện OrderPlacedEvent chứa mã đơn {string} và tổng thanh toán {double} VND")
    public void suKienChuaMaDonVaTong(String orderId, double total) {
        OrderPlacedEvent event = (OrderPlacedEvent) eventPublisher.getEvents().stream()
                .filter(e -> e instanceof OrderPlacedEvent)
                .findFirst()
                .orElse(null);
        assertThat(event).isNotNull();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getTotalPayable()).isEqualTo(total);
    }
}
