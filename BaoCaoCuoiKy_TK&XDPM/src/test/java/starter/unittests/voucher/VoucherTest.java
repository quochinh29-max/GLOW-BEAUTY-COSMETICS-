package starter.unittests.voucher;

import starter.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;

public class VoucherTest {

    @Test
    @DisplayName("Kiểm tra voucher hợp lệ khi đủ điều kiện chi tiêu tối thiểu")
    public void testVoucherValidity_WhenMinSpendSatisfied() {
        Voucher voucher = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5,
                VoucherStatus.ACTIVE);

        assertThat(voucher.isValid(500000.0)).isTrue();
        assertThat(voucher.isValid(200000.0)).isFalse();
    }

    @Test
    @DisplayName("Kiểm tra tính chiết khấu phần trăm có trần")
    public void testCalculateDiscount_PercentageWithMaxCap() {
        Voucher voucher = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5,
                VoucherStatus.ACTIVE);

        // 500k * 10% = 50k > trần 40k => lấy 40k
        assertThat(voucher.calculateDiscount(500000.0)).isEqualTo(40000.0);

        // 350k * 10% = 35k < trần 40k => lấy 35k
        assertThat(voucher.calculateDiscount(350000.0)).isEqualTo(35000.0);
    }

    @Test
    @DisplayName("Kiểm tra tính chiết khấu số tiền cố định")
    public void testCalculateDiscount_FixedAmount() {
        Voucher voucher = new Voucher("FIXED50", DiscountType.FIXED_AMOUNT, 50000.0, null, 200000.0, 5,
                VoucherStatus.ACTIVE);

        assertThat(voucher.calculateDiscount(250000.0)).isEqualTo(50000.0);
    }

    @Test
    @DisplayName("Kiểm tra giảm số lượt sử dụng và cập nhật EXHAUSTED khi về 0")
    public void testDecreaseUsage_TransitionsToExhausted() {
        Voucher voucher = new Voucher("LAST_ONE", DiscountType.FIXED_AMOUNT, 20000.0, null, 100000.0, 1,
                VoucherStatus.ACTIVE);

        voucher.decreaseUsage();

        assertThat(voucher.getRemainingUses()).isEqualTo(0);
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.EXHAUSTED);
        assertThat(voucher.isValid(200000.0)).isFalse();
    }
}