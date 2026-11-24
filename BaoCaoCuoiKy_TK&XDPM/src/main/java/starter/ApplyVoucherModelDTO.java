package starter;

public class ApplyVoucherModelDTO {
    private String orderId;
    private double orderAmount;
    private String voucherCode;
    private OrderStatus orderStatus = OrderStatus.ACTIVE;

    public ApplyVoucherModelDTO(String orderId, double orderAmount, String voucherCode) {
        this.orderId = orderId;
        this.orderAmount = orderAmount;
        this.voucherCode = voucherCode;
        this.orderStatus = OrderStatus.ACTIVE;
    }

    public ApplyVoucherModelDTO(String orderId, double orderAmount, String voucherCode, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.orderAmount = orderAmount;
        this.voucherCode = voucherCode;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.ACTIVE;
    }

    public String getOrderId() { return orderId; }
    public double getOrderAmount() { return orderAmount; }
    public String getVoucherCode() { return voucherCode; }
    public OrderStatus getOrderStatus() { return orderStatus; }
}
