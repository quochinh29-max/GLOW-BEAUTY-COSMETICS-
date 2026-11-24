package starter;

public class ApplyVoucherOutputDTO {
    private boolean success;
    private double discountAmount;
    private double payableAmount;
    private int remainingUses;
    private String message;

    public ApplyVoucherOutputDTO(boolean success, double discountAmount, double payableAmount, int remainingUses, String message) {
        this.success = success;
        this.discountAmount = discountAmount;
        this.payableAmount = payableAmount;
        this.remainingUses = remainingUses;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public double getDiscountAmount() { return discountAmount; }
    public double getPayableAmount() { return payableAmount; }
    public int getRemainingUses() { return remainingUses; }
    public String getMessage() { return message; }
}