package starter;

import java.util.*;

public class InMemoryReviewDBMock implements ReviewStoreAble {

    public static class OrderItem {
        public String productId;
        public boolean reviewed;

        public OrderItem(String productId, boolean reviewed) {
            this.productId = productId;
            this.reviewed = reviewed;
        }
    }

    public Map<String, OrderRecord> getOrders() {
        return orders;
    }

    public static class OrderRecord {
        public String orderId;
        public String customerId;
        public OrderStatus status;
        public List<OrderItem> items = new ArrayList<>();

        public OrderRecord(String orderId, String customerId, OrderStatus status) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.status = status;
        }
    }

    private final Map<String, OrderRecord> orders = new HashMap<>();
    private final List<Review> reviews = new ArrayList<>();
    private boolean simulateError = false;

    public void addOrder(OrderRecord order) {
        orders.put(order.orderId, order);
    }

    public void setSimulateError(boolean simulateError) {
        this.simulateError = simulateError;
    }

    @Override
    public boolean isOrderDelivered(String orderId) {
        OrderRecord ord = orders.get(orderId);
        return ord != null && ord.status == OrderStatus.DELIVERED;
    }

    @Override
    public boolean doesOrderBelongToCustomer(String orderId, String customerId) {
        OrderRecord ord = orders.get(orderId);
        return ord != null && Objects.equals(ord.customerId, customerId);
    }

    @Override
    public boolean isProductInOrder(String orderId, String productId) {
        OrderRecord ord = orders.get(orderId);
        if (ord == null)
            return false;
        return ord.items.stream().anyMatch(i -> i.productId.equals(productId));
    }

    @Override
    public boolean hasProductBeenReviewed(String orderId, String productId) {
        OrderRecord ord = orders.get(orderId);
        if (ord == null)
            return false;
        return ord.items.stream().anyMatch(i -> i.productId.equals(productId) && i.reviewed);
    }

    @Override
    public boolean saveReview(Review review) {
        if (simulateError) {
            return false;
        }
        reviews.add(review);
        return true;
    }

    @Override
    public void updateProductReviewStatus(String orderId, String productId, boolean reviewed) {
        OrderRecord ord = orders.get(orderId);
        if (ord != null) {
            for (OrderItem item : ord.items) {
                if (item.productId.equals(productId)) {
                    item.reviewed = reviewed;
                }
            }
        }
    }

    public List<Review> getReviews() {
        return reviews;
    }
}