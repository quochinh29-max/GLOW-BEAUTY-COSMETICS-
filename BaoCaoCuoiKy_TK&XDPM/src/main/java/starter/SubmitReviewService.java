package starter;

public class SubmitReviewService {
    private final ReviewStoreAble reviewStore;
    private final ReviewPresentAble presenter;
    private final EventPublisher eventPublisher;
    private final ReviewValidationService validationService;

    public SubmitReviewService(ReviewStoreAble reviewStore, ReviewPresentAble presenter, EventPublisher eventPublisher) {
        this.reviewStore = reviewStore;
        this.presenter = presenter;
        this.eventPublisher = eventPublisher;
        this.validationService = new ReviewValidationService();
    }

    public SubmitReviewOutputDTO execute(SubmitReviewModelDTO input) {
        String orderId = input.getOrderId();
        String customerId = input.getCustomerId();
        String productId = input.getProductId();
        double rawRating = input.getRating();
        String comment = input.getComment();

        // 1. Kiểm tra đơn hàng thuộc quyền sở hữu của khách hàng
        if (!reviewStore.doesOrderBelongToCustomer(orderId, customerId)) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Chỉ chủ đơn hàng mới được gửi đánh giá sản phẩm", false, false);
            presenter.format(out);
            return out;
        }

        // 2. Kiểm tra trạng thái đơn hàng (chỉ DELIVERED mới được đánh giá)
        if (!reviewStore.isOrderDelivered(orderId)) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Chỉ đơn hàng đã giao mới được đánh giá", false, false);
            presenter.format(out);
            return out;
        }

        // 3. Kiểm tra sản phẩm có trong đơn hàng
        if (!reviewStore.isProductInOrder(orderId, productId)) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Sản phẩm không thuộc đơn hàng này", false, false);
            presenter.format(out);
            return out;
        }

        // 4. Kiểm tra sản phẩm chưa từng được đánh giá
        if (reviewStore.hasProductBeenReviewed(orderId, productId)) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Sản phẩm đã được đánh giá trong đơn hàng này", false, false);
            presenter.format(out);
            return out;
        }

        // 5. Kiểm tra điểm đánh giá (phải là số nguyên từ 1 đến 5)
        if (!validationService.isIntegerRating(rawRating) || !validationService.isValidRatingRange((int) rawRating)) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Điểm đánh giá phải là số nguyên từ 1 đến 5", false, false);
            presenter.format(out);
            return out;
        }

        // 6. Kiểm tra nhận xét không rỗng hoặc chỉ khoảng trắng
        if (!validationService.isValidComment(comment)) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Nội dung nhận xét không hợp lệ", false, false);
            presenter.format(out);
            return out;
        }

        int rating = (int) rawRating;
        Review review = new Review(orderId, productId, customerId, rating, comment);

        // 7. Lưu đánh giá (xử lý lỗi lưu DB)
        boolean saveSuccess = reviewStore.saveReview(review);
        if (!saveSuccess) {
            SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(false, "Lỗi hệ thống khi lưu đánh giá", false, false);
            presenter.format(out);
            return out;
        }

        // 8. Cập nhật trạng thái đánh giá sản phẩm & Phát hành Domain Event
        reviewStore.updateProductReviewStatus(orderId, productId, true);
        eventPublisher.publish(new ProductReviewSubmittedEvent(orderId, productId, customerId, rating));

        SubmitReviewOutputDTO out = new SubmitReviewOutputDTO(true, "Cảm ơn bạn đã đánh giá sản phẩm", true, true);
        presenter.format(out);
        return out;
    }
}