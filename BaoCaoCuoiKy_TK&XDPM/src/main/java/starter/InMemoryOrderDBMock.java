package starter;

import java.util.HashMap;
import java.util.Map;

public class InMemoryOrderDBMock implements OrderStoreAble {
    public static class OrderData {
        public String orderId;
        public String customerId;
        public OrderStatus status;
        public PlaceOrderModelDTO orderModel;
        public double totalPayable;

        public OrderData(String orderId, String customerId, OrderStatus status) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.status = status;
        }
    }

    private final Map<String, OrderData> orders = new HashMap<>();
    private final Map<String, Integer> stock = new HashMap<>();
    private boolean simulateError = false;

    public void setSimulateError(boolean simulateError) {
        this.simulateError = simulateError;
    }

    public void addOrder(String orderId, String customerId, OrderStatus status) {
        orders.put(orderId, new OrderData(orderId, customerId, status));
    }

    public void addStock(String productId, int quantity) {
        stock.put(productId, quantity);
    }

    @Override
    public boolean doesOrderBelongToCustomer(String orderId, String customerId) {
        OrderData order = orders.get(orderId);
        if (order == null) {
            return false;
        }
        return order.customerId != null && order.customerId.equals(customerId);
    }

    @Override
    public OrderStatus getOrderStatus(String orderId) {
        OrderData order = orders.get(orderId);
        return order != null ? order.status : null;
    }

    @Override
    public int getStock(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    @Override
    public boolean saveOrder(PlaceOrderModelDTO order, double totalPayable) {
        if (simulateError) {
            return false;
        }
        OrderData orderData = orders.computeIfAbsent(order.getOrderId(),
                id -> new OrderData(id, order.getCustomerId(), OrderStatus.ACTIVE));
        orderData.orderModel = order;
        orderData.totalPayable = totalPayable;
        return true;
    }

    @Override
    public boolean deductStock(String productId, int quantity) {
        int current = getStock(productId);
        if (current < quantity) {
            return false;
        }
        stock.put(productId, current - quantity);
        return true;
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus status) {
        OrderData order = orders.get(orderId);
        if (order != null) {
            order.status = status;
        }
    }
}
