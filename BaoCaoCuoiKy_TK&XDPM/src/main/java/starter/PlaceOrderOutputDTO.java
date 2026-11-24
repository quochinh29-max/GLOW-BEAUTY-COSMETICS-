package starter;

public class PlaceOrderOutputDTO {
    private boolean success;
    private double subtotal;
    private double discountAmount;
    private double shippingFee;
    private double totalPayable;
    private OrderStatus orderStatus;
    private String message;

    public PlaceOrderOutputDTO(boolean success, double subtotal, double discountAmount, double shippingFee,
                               double totalPayable, OrderStatus orderStatus, String message) {
        this.success = success;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.shippingFee = shippingFee;
        this.totalPayable = totalPayable;
        this.orderStatus = orderStatus;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public double getTotalPayable() {
        return totalPayable;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public String getMessage() {
        return message;
    }
}
