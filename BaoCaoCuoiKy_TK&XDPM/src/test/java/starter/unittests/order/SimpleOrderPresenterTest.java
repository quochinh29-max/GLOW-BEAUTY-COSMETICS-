package starter.unittests.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.OrderStatus;
import starter.PlaceOrderOutputDTO;
import starter.SimpleOrderPresenter;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleOrderPresenterTest {

    @Test
    @DisplayName("Lưu và truy xuất output gần nhất trong Presenter")
    public void testPresenterFormat() {
        SimpleOrderPresenter presenter = new SimpleOrderPresenter();
        PlaceOrderOutputDTO output = new PlaceOrderOutputDTO(true, 500000.0, 50000.0, 0.0, 450000.0,
                OrderStatus.PAID, "Đặt hàng thành công");

        presenter.format(output);

        assertThat(presenter.getLastOutput()).isNotNull();
        assertThat(presenter.getLastOutput().isSuccess()).isTrue();
        assertThat(presenter.getLastOutput().getTotalPayable()).isEqualTo(450000.0);
    }
}
