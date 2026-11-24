package starter.unittests.voucher;

import starter.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.ApplyVoucherOutputDTO;
import starter.SimpleVoucherPresenter;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleVoucherPresenterTest {

    @Test
    @DisplayName("Presenter định dạng và giữ lại kết quả Output DTO chính xác")
    public void testFormatOutputDTO() {
        SimpleVoucherPresenter presenter = new SimpleVoucherPresenter();
        ApplyVoucherOutputDTO dto = new ApplyVoucherOutputDTO(true, 40000.0, 460000.0, 9, "Thành công");

        presenter.format(dto);

        assertThat(presenter.getLastOutput()).isNotNull();
        assertThat(presenter.getLastOutput().isSuccess()).isTrue();
        assertThat(presenter.getLastOutput().getDiscountAmount()).isEqualTo(40000.0);
        assertThat(presenter.getLastOutput().getPayableAmount()).isEqualTo(460000.0);
        assertThat(presenter.getLastOutput().getRemainingUses()).isEqualTo(9);
    }
}