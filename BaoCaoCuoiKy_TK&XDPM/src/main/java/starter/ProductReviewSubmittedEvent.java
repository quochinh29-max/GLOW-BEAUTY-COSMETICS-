package starter;

import java.time.LocalDateTime;

public class ProductReviewSubmittedEvent implements DomainEvent {
    private final String orderId;
    private final String productId;
    private final String customerId;
    private final int rating;
    private final LocalDateTime timestamp;

    public ProductReviewSubmittedEvent(String orderId, String productId, String customerId, int rating) {
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.rating = rating;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getCustomerId() { return customerId; }
    public int getRating() { return rating; }

    @Override
    public LocalDateTime occurredOn() {
        return timestamp;
    }
}