package starter;

public class SubmitReviewModelDTO {
    private String customerId;
    private String orderId;
    private String productId;
    private double rating;
    private String comment;

    public SubmitReviewModelDTO(String customerId, String orderId, String productId, double rating, String comment) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getCustomerId() { return customerId; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public double getRating() { return rating; }
    public String getComment() { return comment; }
}