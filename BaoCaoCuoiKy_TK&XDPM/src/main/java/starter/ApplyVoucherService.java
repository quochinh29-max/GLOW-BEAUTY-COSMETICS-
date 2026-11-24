package starter;

public class ApplyVoucherService {
    private final VoucherStoreAble voucherStore;
    private final VoucherPresentAble presenter;
    private final EventPublisher eventPublisher;
    private final VoucherCalculationService calculationService;

    public ApplyVoucherService(VoucherStoreAble voucherStore, VoucherPresentAble presenter, EventPublisher eventPublisher) {
        this.voucherStore = voucherStore;
        this.presenter = presenter;
        this.eventPublisher = eventPublisher;
        this.calculationService = new VoucherCalculationService();
    }

    public ApplyVoucherOutputDTO execute(ApplyVoucherModelDTO input) {
        double originalAmount = input.getOrderAmount();
        String code = input.getVoucherCode();
        String orderId = input.getOrderId();

        // 0. Kiểm tra trạng thái đơn hàng (chỉ áp dụng cho đơn hàng đang hoạt động ACTIVE)
        if (input.getOrderStatus() != null && input.getOrderStatus() != OrderStatus.ACTIVE) {
            Voucher voucher = voucherStore.findVoucher(code);
            int remaining = (voucher != null) ? voucher.getRemainingUses() : 0;
            ApplyVoucherOutputDTO out = new ApplyVoucherOutputDTO(false, 0.0, originalAmount, remaining, "Hệ thống từ chối áp dụng voucher");
            presenter.format(out);
            return out;
        }

        // 1. Kiểm tra không được áp dụng trùng lặp cho cùng một đơn hàng
        if (voucherStore.isVoucherApplied(orderId, code)) {
            ApplyVoucherOutputDTO out = new ApplyVoucherOutputDTO(false, 0.0, originalAmount, 0, "Hệ thống từ chối yêu cầu");
            presenter.format(out);
            return out;
        }

        // 2. Tìm voucher trong database
        Voucher voucher = voucherStore.findVoucher(code);
        if (voucher == null || !voucher.isValid(originalAmount)) {
            int remaining = (voucher != null) ? voucher.getRemainingUses() : 0;
            ApplyVoucherOutputDTO out = new ApplyVoucherOutputDTO(false, 0.0, originalAmount, remaining, "Hệ thống từ chối áp dụng voucher");
            presenter.format(out);
            return out;
        }

        // 3. Sử dụng Sub-service tính toán giảm giá
        double discount = calculationService.calculateDiscount(voucher, originalAmount);
        double payable = originalAmount - discount;

        // 4. Lưu dữ liệu an toàn (kiểm tra lỗi lưu DB)
        boolean saveSuccess = voucherStore.saveAppliedVoucher(orderId, code, discount);
        if (!saveSuccess) {
            ApplyVoucherOutputDTO out = new ApplyVoucherOutputDTO(false, 0.0, originalAmount, voucher.getRemainingUses(), "Hệ thống thông báo xử lý thất bại");
            presenter.format(out);
            return out;
        }

        // 5. Cập nhật lượt dùng và phát hành Domain Event
        voucher.decreaseUsage();
        eventPublisher.publish(new VoucherAppliedEvent(orderId, code, discount));

        ApplyVoucherOutputDTO out = new ApplyVoucherOutputDTO(true, discount, payable, voucher.getRemainingUses(), "Áp dụng voucher thành công");
        presenter.format(out);
        return out;
    }
}