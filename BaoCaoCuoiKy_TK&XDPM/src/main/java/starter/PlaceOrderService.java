package starter;

import java.util.List;

public class PlaceOrderService {
    private final OrderStoreAble orderStore;
    private final VoucherStoreAble voucherStore;
    private final OrderPresentAble presenter;
    private final EventPublisher eventPublisher;
    private final OrderValidationService validationService;
    private final OrderPricingService pricingService;

    public PlaceOrderService(OrderStoreAble orderStore, VoucherStoreAble voucherStore,
                             OrderPresentAble presenter, EventPublisher eventPublisher) {
        this.orderStore = orderStore;
        this.voucherStore = voucherStore;
        this.presenter = presenter;
        this.eventPublisher = eventPublisher;
        this.validationService = new OrderValidationService();
        this.pricingService = new OrderPricingService();
    }

    public PlaceOrderOutputDTO execute(PlaceOrderModelDTO input) {
        String orderId = input.getOrderId();
        String customerId = input.getCustomerId();
        List<OrderLine> items = input.getItems();
        OrderStatus currentStatus = orderStore.getOrderStatus(orderId);
        if (currentStatus == null) {
            currentStatus = input.getOrderStatus() != null ? input.getOrderStatus() : OrderStatus.ACTIVE;
        }

        // Rule 1: Giỏ hàng phải có ít nhất một sản phẩm
        if (items == null || items.isEmpty()) {
            return fail(0.0, 0.0, 0.0, 0.0, currentStatus, "Giỏ hàng không có sản phẩm");
        }

        // Rule 2: Chỉ chủ giỏ hàng mới được đặt hàng
        if (!orderStore.doesOrderBelongToCustomer(orderId, customerId)) {
            return fail(0.0, 0.0, 0.0, 0.0, currentStatus, "Chỉ chủ giỏ hàng mới được đặt hàng");
        }

        // Rule 3: Chỉ đơn hàng đang hoạt động mới được đặt (ACTIVE)
        if (currentStatus != OrderStatus.ACTIVE) {
            return fail(0.0, 0.0, 0.0, 0.0, currentStatus, "Chỉ đơn hàng đang hoạt động mới được đặt hàng");
        }

        // Rule 4: Số lượng mỗi sản phẩm phải là số nguyên dương và không vượt quá tồn kho
        for (OrderLine item : items) {
            int stock = orderStore.getStock(item.getProductId());
            if (!validationService.isValidQuantity(item.getQuantity(), stock)) {
                return fail(0.0, 0.0, 0.0, 0.0, currentStatus,
                        "Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho");
            }
        }

        // Rule 5: Thông tin giao hàng phải đầy đủ và hợp lệ
        if (!validationService.isValidShipping(input.getShippingInfo())) {
            return fail(0.0, 0.0, 0.0, 0.0, currentStatus, "Thông tin giao hàng không hợp lệ");
        }

        // Rule 7: Tính toán tiền
        double subtotal = pricingService.calculateSubtotal(items);
        double discountAmount = input.getDiscountAmount();

        // Rule 8: Kiểm tra voucher & số tiền giảm giá
        String voucherCode = input.getVoucherCode();
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            if (!voucherStore.isVoucherApplied(orderId, voucherCode)) {
                return fail(subtotal, 0.0, 0.0, 0.0, currentStatus,
                        "Voucher chưa được áp dụng cho đơn hàng này");
            }
        }

        if (discountAmount < 0 || discountAmount > subtotal) {
            return fail(subtotal, discountAmount, 0.0, 0.0, currentStatus,
                    "Số tiền giảm giá không hợp lệ");
        }

        double shippingFee = pricingService.calculateShippingFee(subtotal);
        double totalPayable = pricingService.calculateTotalPayable(subtotal, discountAmount, shippingFee);

        // Rule 6: Thanh toán COD chỉ áp dụng cho đơn có tổng phải trả tối đa 20,000,000 VND
        if (input.getPaymentMethod() == PaymentMethod.COD && totalPayable > 20000000.0) {
            return fail(subtotal, discountAmount, shippingFee, totalPayable, currentStatus,
                    "Thanh toán COD chỉ áp dụng cho đơn tối đa 20000000 VND");
        }

        // Lưu đơn hàng
        boolean saved = orderStore.saveOrder(input, totalPayable);
        if (!saved) {
            return fail(subtotal, discountAmount, shippingFee, totalPayable, currentStatus,
                    "Lỗi hệ thống khi lưu đơn hàng");
        }

        // Trừ tồn kho
        for (OrderLine item : items) {
            orderStore.deductStock(item.getProductId(), item.getQuantity());
        }

        // Chuyển trạng thái sang PAID
        orderStore.updateOrderStatus(orderId, OrderStatus.PAID);

        // Phát hành domain event OrderPlacedEvent
        eventPublisher.publish(new OrderPlacedEvent(orderId, totalPayable));

        PlaceOrderOutputDTO output = new PlaceOrderOutputDTO(true, subtotal, discountAmount,
                shippingFee, totalPayable, OrderStatus.PAID, "Đặt hàng thành công");
        presenter.format(output);
        return output;
    }

    private PlaceOrderOutputDTO fail(double subtotal, double discount, double shipping,
                                     double total, OrderStatus status, String message) {
        PlaceOrderOutputDTO out = new PlaceOrderOutputDTO(false, subtotal, discount, shipping, total, status, message);
        presenter.format(out);
        return out;
    }
}
