package starter.unittests.voucher;

import starter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplyVoucherServiceTest {

    private InMemoryVoucherDBMock dbMock;
    private SimpleVoucherPresenter presenter;
    private InMemoryEventPublisherMock eventPublisher;
    private ApplyVoucherService service;

    @BeforeEach
    public void setUp() {
        dbMock = new InMemoryVoucherDBMock();
        presenter = new SimpleVoucherPresenter();
        eventPublisher = new InMemoryEventPublisherMock();
        service = new ApplyVoucherService(dbMock, presenter, eventPublisher);
    }

    @Test
    @DisplayName("Áp dụng voucher hợp lệ thành công và phát hành Domain Event")
    public void testExecute_Success() {
        Voucher v = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);

        ApplyVoucherModelDTO input = new ApplyVoucherModelDTO("ORD-01", 500000.0, "SALE10");
        ApplyVoucherOutputDTO output = service.execute(input);

        assertThat(output.isSuccess()).isTrue();
        assertThat(output.getDiscountAmount()).isEqualTo(40000.0);
        assertThat(output.getPayableAmount()).isEqualTo(460000.0);
        assertThat(output.getRemainingUses()).isEqualTo(4);
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isTrue();
    }

    @Test
    @DisplayName("Từ chối nếu voucher đã được áp dụng trước đó cho đơn hàng")
    public void testExecute_RejectDuplicateApplication() {
        Voucher v = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
        dbMock.saveAppliedVoucher("ORD-01", "SALE10", 40000.0);

        ApplyVoucherModelDTO input = new ApplyVoucherModelDTO("ORD-01", 500000.0, "SALE10");
        ApplyVoucherOutputDTO output = service.execute(input);

        assertThat(output.isSuccess()).isFalse();
        assertThat(output.getPayableAmount()).isEqualTo(500000.0);
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isFalse();
    }

    @Test
    @DisplayName("Xử lý an toàn khi lưu DB gặp lỗi: không trừ lượt, không phát event")
    public void testExecute_RollbackWhenDBSaveFails() {
        Voucher v = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);
        dbMock.setSimulateError(true);

        ApplyVoucherModelDTO input = new ApplyVoucherModelDTO("ORD-01", 500000.0, "SALE10");
        ApplyVoucherOutputDTO output = service.execute(input);

        assertThat(output.isSuccess()).isFalse();
        assertThat(output.getPayableAmount()).isEqualTo(500000.0);
        assertThat(output.getRemainingUses()).isEqualTo(5); // Số lượt không đổi
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isFalse();
    }

    @Test
    @DisplayName("Từ chối áp dụng voucher khi trạng thái đơn hàng không còn hoạt động (PAID hoặc CANCELLED)")
    public void testExecute_RejectWhenOrderNotActive() {
        Voucher v = new Voucher("SALE10", DiscountType.PERCENTAGE, 10, 40000.0, 300000.0, 5, VoucherStatus.ACTIVE);
        dbMock.addVoucher(v);

        // Đơn hàng đã thanh toán PAID
        ApplyVoucherModelDTO inputPaid = new ApplyVoucherModelDTO("ORD-01", 500000.0, "SALE10", OrderStatus.PAID);
        ApplyVoucherOutputDTO outputPaid = service.execute(inputPaid);

        assertThat(outputPaid.isSuccess()).isFalse();
        assertThat(outputPaid.getDiscountAmount()).isEqualTo(0.0);
        assertThat(outputPaid.getPayableAmount()).isEqualTo(500000.0);
        assertThat(outputPaid.getRemainingUses()).isEqualTo(5);
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isFalse();

        // Đơn hàng đã hủy CANCELLED
        ApplyVoucherModelDTO inputCancelled = new ApplyVoucherModelDTO("ORD-02", 500000.0, "SALE10", OrderStatus.CANCELLED);
        ApplyVoucherOutputDTO outputCancelled = service.execute(inputCancelled);

        assertThat(outputCancelled.isSuccess()).isFalse();
        assertThat(outputCancelled.getDiscountAmount()).isEqualTo(0.0);
        assertThat(outputCancelled.getPayableAmount()).isEqualTo(500000.0);
        assertThat(outputCancelled.getRemainingUses()).isEqualTo(5);
        assertThat(eventPublisher.hasPublished(VoucherAppliedEvent.class)).isFalse();
    }
}