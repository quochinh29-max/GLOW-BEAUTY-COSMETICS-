package starter;

import java.util.Collections;
import java.util.List;

public class PlaceOrderModelDTO {
    private String orderId;
    private String customerId;
    private List<OrderLine> items;
    private ShippingInfo shippingInfo;
    private PaymentMethod paymentMethod;
    private String voucherCode;
    private double discountAmount;
    private OrderStatus orderStatus = OrderStatus.ACTIVE;

    public PlaceOrderModelDTO(String orderId, String customerId, List<OrderLine> items, ShippingInfo shippingInfo,
                             PaymentMethod paymentMethod, String voucherCode, double discountAmount) {
        this(orderId, customerId, items, shippingInfo, paymentMethod, voucherCode, discountAmount, OrderStatus.ACTIVE);
    }

    public PlaceOrderModelDTO(String orderId, String customerId, List<OrderLine> items, ShippingInfo shippingInfo,
                             PaymentMethod paymentMethod, String voucherCode, double discountAmount, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items != null ? items : Collections.emptyList();
        this.shippingInfo = shippingInfo;
        this.paymentMethod = paymentMethod;
        this.voucherCode = voucherCode;
        this.discountAmount = discountAmount;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.ACTIVE;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public ShippingInfo getShippingInfo() {
        return shippingInfo;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
}
