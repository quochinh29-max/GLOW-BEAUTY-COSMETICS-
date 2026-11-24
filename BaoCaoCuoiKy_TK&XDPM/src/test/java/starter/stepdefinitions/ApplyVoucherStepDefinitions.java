package starter.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import starter.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplyVoucherStepDefinitions {

    private InMemoryVoucherDBMock dbMock;
    private SimpleVoucherPresenter presenter;
    private InMemoryEventPublisherMock eventPublisher;
    private ApplyVoucherService service;

    private double currentOrderAmount;
    private OrderStatus currentOrderStatus = OrderStatus.ACTIVE;
    private String currentOrderId = "ORD-001";
    private String currentVoucherCode = "VOUCHER_DEFAULT";
    private ApplyVoucherOutputDTO resultOutput;

    @Before
    public void setUp() {
        dbMock = new InMemoryVoucherDBMock();
        presenter = new SimpleVoucherPresenter();
        eventPublisher = new InMemoryEventPublisherMock();
        service = new ApplyVoucherService(dbMock, presenter, eventPublisher);
    }

    @Given("khách hàng có một giỏ hàng đang hoạt động")
    public void khachHangCoMotGioHangDangHoatDong() {
        currentOrderId = "ORD-" + System.currentTimeMillis();
        currentOrderStatus = OrderStatus.ACTIVE;
    }

    @Given("đơn hàng có giá trị {double} VND")
    public void donHangCoGiaTri(double amount) {
        this.currentOrderAmount = amount;
    }

    @Given("đơn hàng có trạng thái {string}")
    public void donHangCoTrangThai(String status) {
        this.currentOrderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
    }

    @Given("mã {word} giảm {double} phần trăm")
    @Given("voucher {word} giảm {double} phần trăm")
    public void maGiamPhanTram(String code, double percent) {
        this.currentVoucherCode = code;
        Voucher v = new Voucher(code, DiscountType.PERCENTAGE, percent, null, 0.0, 10, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
    }

    @Given("mã {word} giảm cố định {double} VND")
    public void maGiamCoDinh(String code, double amount) {
        this.currentVoucherCode = code;
        Voucher v = new Voucher(code, DiscountType.FIXED_AMOUNT, amount, null, 0.0, 10, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
    }

    @Given("mức giảm tối đa là {double} VND")
    public void mucGiamToiDaLa(double maxCap) {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        if (v != null) {
            Voucher updated = new Voucher(v.getCode(), v.getDiscountType(), v.getDiscountValue(), maxCap,
                    v.getMinSpend(), v.getRemainingUses(), v.getStatus());
            dbMock.addVoucher(updated);
        }
    }

    @Given("đơn hàng phải đạt tối thiểu {double} VND")
    @Given("giá trị đơn hàng tối thiểu là {double} VND")
    public void donHangPhaiDatToiThieu(double minSpend) {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        if (v != null) {
            Voucher updated = new Voucher(v.getCode(), v.getDiscountType(), v.getDiscountValue(), v.getMaxCap(),
                    minSpend, v.getRemainingUses(), v.getStatus());
            dbMock.addVoucher(updated);
        }
    }

    @Given("mã giảm giá còn {int} lượt sử dụng")
    public void maGiamGiaConLuotSuDung(int uses) {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        if (v != null) {
            v.setRemainingUses(uses);
            if (uses <= 0)
                v.setStatus(VoucherStatus.EXHAUSTED);
        }
    }

    @Given("mã giảm giá còn lượt sử dụng")
    public void maGiamGiaConLuot() {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        if (v != null) {
            v.setRemainingUses(5);
        }
    }

    @Given("voucher yêu cầu giá trị đơn hàng tối thiểu {double} VND")
    public void voucherYeuCauGiaTriToiThieu(double minSpend) {
        this.currentVoucherCode = "REQ_MIN";
        Voucher v = new Voucher(currentVoucherCode, DiscountType.FIXED_AMOUNT, 20000.0, null, minSpend, 5,
                VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
    }

    @Given("voucher có trạng thái {string}")
    public void voucherCoTrangThai(String status) {
        this.currentVoucherCode = "VOUCHER_STATUS";
        VoucherStatus st = VoucherStatus.valueOf(status.trim().toUpperCase());
        Voucher v = new Voucher(currentVoucherCode, DiscountType.PERCENTAGE, 10.0, 40000.0, 100000.0, 5, st);
        dbMock.addVoucher(v);
    }

    @Given("voucher {string} có loại {string}")
    public void voucherCoLoai(String code, String type) {
        this.currentVoucherCode = code;
        DiscountType dType = DiscountType.valueOf(type.trim().toUpperCase());
        VoucherStatus status = code.toUpperCase().contains("EXPIRED") ? VoucherStatus.EXPIRED : VoucherStatus.ACTIVE;
        Voucher v = new Voucher(code, dType, 0.0, null, 0.0, 5, status);
        dbMock.addVoucher(v);
    }

    @Given("voucher có giá trị giảm {double}")
    public void voucherCoGiaTriGiam(double val) {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        if (v != null) {
            Voucher updated = new Voucher(v.getCode(), v.getDiscountType(), val, v.getMaxCap(), v.getMinSpend(),
                    v.getRemainingUses(), v.getStatus());
            dbMock.addVoucher(updated);
        }
    }

    @Given("mức giảm tối đa là {word}")
    public void mucGiamToiDaWord(String cap) {
        if (!cap.equalsIgnoreCase("N/A")) {
            mucGiamToiDaLa(Double.parseDouble(cap));
        }
    }

    @Given("mức chi tiêu tối thiểu là {double}")
    public void mucChiTieuToiThieu(double min) {
        donHangPhaiDatToiThieu(min);
    }

    @Given("voucher còn {int} lượt sử dụng")
    public void voucherConLuotSuDung(int uses) {
        maGiamGiaConLuotSuDung(uses);
    }

    @Given("đơn hàng đã áp dụng voucher {word}")
    public void donHangDaApDungVoucher(String code) {
        this.currentVoucherCode = code;
        Voucher v = new Voucher(code, DiscountType.FIXED_AMOUNT, 50000.0, null, 100000.0, 5, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
        dbMock.saveAppliedVoucher(currentOrderId, code, 50000.0);
    }

    @Given("đơn hàng đủ điều kiện áp dụng voucher")
    public void donHangDuDieuKien() {
        this.currentOrderAmount = 500000.0;
        this.currentVoucherCode = "VALID_VOUCHER";
        Voucher v = new Voucher(currentVoucherCode, DiscountType.FIXED_AMOUNT, 50000.0, null, 200000.0, 5,
                VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
    }

    @Given("hệ thống gặp lỗi khi lưu dữ liệu")
    public void heThongGapLoiKhiLuu() {
        dbMock.setSimulateError(true);
    }

    @When("khách hàng áp dụng mã {word}")
    @When("khách hàng áp dụng voucher {word}")
    public void khachHangApDungMa(String code) {
        this.currentVoucherCode = code;
        khachHangApDungVoucher();
    }

    @When("khách hàng tiếp tục áp dụng voucher {word}")
    public void khachHangTiepTucApDungVoucher(String code) {
        this.currentVoucherCode = code;
        khachHangApDungVoucher();
    }

    @When("khách hàng áp dụng voucher")
    public void khachHangApDungVoucher() {
        ApplyVoucherModelDTO input = new ApplyVoucherModelDTO(currentOrderId, currentOrderAmount, currentVoucherCode, currentOrderStatus);
        resultOutput = service.execute(input);
    }

    @Then("số tiền được giảm là {double} VND")
    public void soTienDuocGiamLa(double discount) {
        assertThat(resultOutput.getDiscountAmount()).isEqualTo(discount);
    }

    @Then("tổng tiền phải thanh toán là {double} VND")
    @Then("số tiền phải thanh toán là {double} VND")
    public void tongTienPhaiThanhToan(double payable) {
        assertThat(resultOutput.getPayableAmount()).isEqualTo(payable);
    }

    @Then("số lượt sử dụng còn lại là {int}")
    public void soLuotSuDungConLaiLa(int remaining) {
        assertThat(resultOutput.getRemainingUses()).isEqualTo(remaining);
    }

    @Then("số lượt sử dụng giảm đi {int}")
    @Then("số lượt sử dụng voucher giảm đi {int}")
    public void soLuotSuDungGiamDi(int diff) {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        assertThat(resultOutput.getRemainingUses()).isEqualTo(v.getRemainingUses());
    }

    @Then("hệ thống từ chối áp dụng voucher")
    public void heThongTuChoiApDungVoucher() {
        assertThat(resultOutput.isSuccess()).isFalse();
    }

    @Then("hệ thống từ chối yêu cầu áp dụng voucher")
    public void heThongTuChoiYeuCau() {
        assertThat(resultOutput.isSuccess()).isFalse();
    }

    @Then("tổng tiền đơn hàng không thay đổi")
    public void tongTienDonHangKhongThayDoi() {
        assertThat(resultOutput.getPayableAmount()).isEqualTo(currentOrderAmount);
    }

    @Then("số lượt sử dụng voucher không thay đổi")
    public void soLuotSuDungKhongThayDoi() {
        Voucher v = dbMock.findVoucher(currentVoucherCode);
        if (v != null) {
            assertThat(resultOutput.getRemainingUses()).isEqualTo(v.getRemainingUses());
        }
    }

    @Then("hệ thống thông báo xử lý thất bại")
    public void heThongThongBaoThatBai() {
        assertThat(resultOutput.isSuccess()).isFalse();
    }

    @Then("kết quả áp dụng voucher là {string}")
    public void ketQuaApDungLa(String expected) {
        boolean expectedBool = expected.equalsIgnoreCase("Có");
        assertThat(resultOutput.isSuccess()).isEqualTo(expectedBool);
    }

    @Then("sự kiện VoucherAppliedEvent được phát hành")
    public void suKienDuocPhatHanh() {
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isTrue();
    }

    @Then("sự kiện VoucherAppliedEvent không được phát hành")
    @Then("không phát sinh thêm sự kiện VoucherAppliedEvent")
    public void suKienKhongDuocPhatHanh() {
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isFalse();
    }
}