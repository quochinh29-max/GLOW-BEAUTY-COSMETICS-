package starter;

public class Review {
    private String orderId;
    private String productId;
    private String customerId;
    private int rating;
    private String comment;

    public Review(String orderId, String productId, String customerId, int rating, String comment) {
        this.orderId = orderId;
        this.productId = productId;
        this.customerId = customerId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getCustomerId() { return customerId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}