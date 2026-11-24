package starter;

public interface OrderStoreAble {
    boolean doesOrderBelongToCustomer(String orderId, String customerId);
    OrderStatus getOrderStatus(String orderId);
    int getStock(String productId);
    boolean saveOrder(PlaceOrderModelDTO order, double totalPayable);
    boolean deductStock(String productId, int quantity);
    void updateOrderStatus(String orderId, OrderStatus status);
}
