package starter;

public class VoucherCalculationService {

    public double calculateDiscount(Voucher voucher, double orderAmount) {
        if (voucher == null || !voucher.isValid(orderAmount)) {
            return 0.0;
        }

        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            double calculated = orderAmount * (voucher.getDiscountValue() / 100.0);
            Double cap = voucher.getMaxCap();
            if (cap != null && cap > 0 && calculated > cap) {
                return cap;
            }
            return calculated;
        } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            return Math.min(voucher.getDiscountValue(), orderAmount);
        }

        return 0.0;
    }
}