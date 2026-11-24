package starter;

public class Voucher {
    private String code;
    private DiscountType discountType;
    private double discountValue;
    private Double maxCap;
    private double minSpend;
    private int remainingUses;
    private VoucherStatus status;

    public Voucher(String code, DiscountType discountType, double discountValue,
            Double maxCap, double minSpend, int remainingUses, VoucherStatus status) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxCap = maxCap;
        this.minSpend = minSpend;
        this.remainingUses = remainingUses;
        this.status = status;
    }

    public boolean isValid(double orderAmount) {
        if (this.status != VoucherStatus.ACTIVE) {
            return false;
        }
        if (this.remainingUses <= 0) {
            return false;
        }
        if (orderAmount < this.minSpend) {
            return false;
        }
        return true;
    }

    public void decreaseUsage() {
        if (this.remainingUses > 0) {
            this.remainingUses--;
            if (this.remainingUses == 0) {
                this.status = VoucherStatus.EXHAUSTED;
            }
        }
    }

    public double calculateDiscount(double orderAmount) {
        if (!isValid(orderAmount)) {
            return 0.0;
        }
        if (discountType == DiscountType.PERCENTAGE) {
            double calculated = orderAmount * (discountValue / 100.0);
            if (maxCap != null && maxCap > 0 && calculated > maxCap) {
                return maxCap;
            }
            return calculated;
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            return Math.min(discountValue, orderAmount);
        }
        return 0.0;
    }

    // Getters & Setters
    public String getCode() {
        return code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public Double getMaxCap() {
        return maxCap;
    }

    public double getMinSpend() {
        return minSpend;
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    public void setRemainingUses(int remainingUses) {
        this.remainingUses = remainingUses;
    }

    public VoucherStatus getStatus() {
        return status;
    }

    public void setStatus(VoucherStatus status) {
        this.status = status;
    }
}