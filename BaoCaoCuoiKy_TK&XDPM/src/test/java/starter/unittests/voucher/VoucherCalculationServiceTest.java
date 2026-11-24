package starter.unittests.voucher;

import starter.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.DiscountType;
import starter.Voucher;
import starter.VoucherCalculationService;
import starter.VoucherStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class VoucherCalculationServiceTest {

    private final VoucherCalculationService calculator = new VoucherCalculationService();

    @Test
    @DisplayName("Tính toán trả về 0 nếu voucher null hoặc không hợp lệ")
    public void testCalculateDiscount_InvalidVoucher() {
        // Trường hợp voucher null
        assertThat(calculator.calculateDiscount(null, 500000.0)).isEqualTo(0.0);

        // Trường hợp voucher đã hết hạn (EXPIRED)
        Voucher expired = new Voucher("EXPIRED10", DiscountType.PERCENTAGE, 10, 40000.0, 100000.0, 5,
                VoucherStatus.EXPIRED);
        assertThat(calculator.calculateDiscount(expired, 500000.0)).isEqualTo(0.0);

        // Trường hợp voucher đã hết lượt sử dụng (EXHAUSTED)
        Voucher exhausted = new Voucher("FIXED50", DiscountType.FIXED_AMOUNT, 50000.0, null, 400000.0, 0,
                VoucherStatus.EXHAUSTED);
        assertThat(calculator.calculateDiscount(exhausted, 600000.0)).isEqualTo(0.0);

        // Trường hợp đơn hàng chưa đạt mức chi tiêu tối thiểu (Min Spend)
        Voucher minSpendVoucher = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5,
                VoucherStatus.ACTIVE);
        assertThat(calculator.calculateDiscount(minSpendVoucher, 200000.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Tính toán giảm giá phần trăm có áp dụng mức giảm tối đa (Max Cap)")
    public void testCalculateDiscount_PercentageWithMaxCap() {
        Voucher voucher = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5,
                VoucherStatus.ACTIVE);

        // Đơn 500k -> 10% = 50k > trần 40k -> Giảm đúng 40k
        assertThat(calculator.calculateDiscount(voucher, 500000.0)).isEqualTo(40000.0);

        // Đơn 350k -> 10% = 35k < trần 40k -> Giảm 35k
        assertThat(calculator.calculateDiscount(voucher, 350000.0)).isEqualTo(35000.0);
    }

    @Test
    @DisplayName("Áp dụng đúng mức giảm phần trăm và không vượt mức giảm tối đa")
    public void testCalculateDiscount_PercentageWithFeatureExampleCap() {
        Voucher voucher = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 400000.0, 300000.0, 10,
                VoucherStatus.ACTIVE);

        // Theo Feature: đơn 1.000.000 VND giảm 10% = 100.000 VND, dưới trần 400.000 VND.
        assertThat(calculator.calculateDiscount(voucher, 1000000.0)).isEqualTo(100000.0);

        // Khi phần trăm tính ra vượt trần, mức giảm phải được giới hạn ở 400.000 VND.
        assertThat(calculator.calculateDiscount(voucher, 5000000.0)).isEqualTo(400000.0);
    }

    @Test
    @DisplayName("Tính toán giảm giá theo số tiền cố định (Fixed Amount)")
    public void testCalculateDiscount_FixedAmount() {
        Voucher voucher = new Voucher("FLAT100", DiscountType.FIXED_AMOUNT, 100000.0, null, 200000.0, 2,
                VoucherStatus.ACTIVE);

        // Đơn 500k -> Giảm cố định 100k
        assertThat(calculator.calculateDiscount(voucher, 500000.0)).isEqualTo(100000.0);
    }
}