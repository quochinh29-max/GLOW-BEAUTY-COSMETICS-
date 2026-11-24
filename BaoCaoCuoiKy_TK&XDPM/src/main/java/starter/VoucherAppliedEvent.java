package starter;

import java.time.LocalDateTime;

public class VoucherAppliedEvent implements DomainEvent {
    private final String orderId;
    private final String voucherCode;
    private final double discountAmount;
    private final LocalDateTime timestamp;

    public VoucherAppliedEvent(String orderId, String voucherCode, double discountAmount) {
        this.orderId = orderId;
        this.voucherCode = voucherCode;
        this.discountAmount = discountAmount;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public String getVoucherCode() { return voucherCode; }
    public double getDiscountAmount() { return discountAmount; }

    @Override
    public LocalDateTime occurredOn() {
        return timestamp;
    }
}