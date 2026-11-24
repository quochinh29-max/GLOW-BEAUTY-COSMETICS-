package starter.unittests.voucher;

import starter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.DiscountType;
import starter.InMemoryVoucherDBMock;
import starter.Voucher;
import starter.VoucherStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryVoucherDBMockTest {

    private InMemoryVoucherDBMock dbMock;

    @BeforeEach
    public void setUp() {
        dbMock = new InMemoryVoucherDBMock();
    }

    @Test
    @DisplayName("Kiểm tra thêm và tìm kiếm voucher theo mã code")
    public void testAddAndFindVoucher() {
        Voucher voucher = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5,
                VoucherStatus.ACTIVE);
        dbMock.addVoucher(voucher);

        Voucher found = dbMock.findVoucher("SALE10");
        assertThat(found).isNotNull();
        assertThat(found.getCode()).isEqualTo("SALE10");

        // Tìm mã không tồn tại
        assertThat(dbMock.findVoucher("NOT_FOUND")).isNull();
    }

    @Test
    @DisplayName("Kiểm tra lưu lịch sử áp dụng voucher và phát hiện trùng lặp cho đơn hàng")
    public void testSaveAndCheckVoucherApplication() {
        String orderId = "ORD-001";
        String voucherCode = "SALE10";

        // Ban đầu chưa áp dụng
        assertThat(dbMock.isVoucherApplied(orderId, voucherCode)).isFalse();

        // Lưu bản ghi áp dụng
        boolean saved = dbMock.saveAppliedVoucher(orderId, voucherCode, 40000.0);
        assertThat(saved).isTrue();

        // Sau khi lưu thì trạng thái phải là đã áp dụng
        assertThat(dbMock.isVoucherApplied(orderId, voucherCode)).isTrue();

        // Đơn hàng khác áp dụng cùng mã voucher thì vẫn được phép
        assertThat(dbMock.isVoucherApplied("ORD-002", voucherCode)).isFalse();
    }

    @Test
    @DisplayName("Kiểm tra cơ chế mô phỏng lỗi database khi lưu bản ghi")
    public void testSimulateErrorOnSave() {
        dbMock.setSimulateError(true);

        boolean saved = dbMock.saveAppliedVoucher("ORD-001", "SALE10", 40000.0);

        // Mô phỏng lỗi nên lưu thất bại
        assertThat(saved).isFalse();
        assertThat(dbMock.isVoucherApplied("ORD-001", "SALE10")).isFalse();
    }
}