package starter;

import java.util.List;

public class OrderPricingService {

    public double calculateSubtotal(List<OrderLine> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (OrderLine item : items) {
            sum += item.lineTotal();
        }
        return sum;
    }

    public double calculateShippingFee(double subtotal) {
        // Miễn phí vận chuyển (0 VND) khi tạm tính >= 500,000 VND, ngược lại 30,000 VND
        return subtotal >= 500000.0 ? 0.0 : 30000.0;
    }

    public double calculateTotalPayable(double subtotal, double discountAmount, double shippingFee) {
        double payable = (subtotal - discountAmount) + shippingFee;
        return Math.max(0.0, payable);
    }
}
