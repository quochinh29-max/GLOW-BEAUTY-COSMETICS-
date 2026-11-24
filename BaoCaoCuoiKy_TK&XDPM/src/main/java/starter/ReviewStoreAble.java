package starter;

public interface ReviewStoreAble {
    boolean isOrderDelivered(String orderId);
    boolean doesOrderBelongToCustomer(String orderId, String customerId);
    boolean isProductInOrder(String orderId, String productId);
    boolean hasProductBeenReviewed(String orderId, String productId);
    boolean saveReview(Review review);
    void updateProductReviewStatus(String orderId, String productId, boolean reviewed);
}