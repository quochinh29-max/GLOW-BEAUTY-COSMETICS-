package starter;

import java.time.LocalDateTime;

public class OrderPlacedEvent implements DomainEvent {
    private final String orderId;
    private final double totalPayable;
    private final LocalDateTime occurredOn;

    public OrderPlacedEvent(String orderId, double totalPayable) {
        this.orderId = orderId;
        this.totalPayable = totalPayable;
        this.occurredOn = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotalPayable() {
        return totalPayable;
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
}
