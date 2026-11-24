package starter.unittests.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaceOrderServiceTest {

    private InMemoryOrderDBMock orderDb;
    private InMemoryVoucherDBMock voucherDb;
    private SimpleOrderPresenter presenter;
    private InMemoryEventPublisherMock eventPublisher;
    private PlaceOrderService placeOrderService;

    @BeforeEach
    public void setUp() {
        orderDb = new InMemoryOrderDBMock();
        voucherDb = new InMemoryVoucherDBMock();
        presenter = new SimpleOrderPresenter();
        eventPublisher = new InMemoryEventPublisherMock();
        placeOrderService = new PlaceOrderService(orderDb, voucherDb, presenter, eventPublisher);
    }

    @Test
    @DisplayName("Đặt hàng thành công với COD khi tổng tiền hợp lệ")
    public void testPlaceOrder_Success() {
        orderDb.addOrder("CART-01", "CUS-01", OrderStatus.ACTIVE);
        orderDb.addStock("P1", 10);

        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine("P1", 2, 300000.0));
        ShippingInfo shipping = new ShippingInfo("Nguyen Van A", "0912345678", "12 Nguyen Hue");

        PlaceOrderModelDTO input = new PlaceOrderModelDTO("CART-01", "CUS-01", lines, shipping,
                PaymentMethod.COD, null, 0.0);

        PlaceOrderOutputDTO output = placeOrderService.execute(input);

        assertThat(output.isSuccess()).isTrue();
        assertThat(output.getSubtotal()).isEqualTo(600000.0);
        assertThat(output.getShippingFee()).isEqualTo(0.0);
        assertThat(output.getTotalPayable()).isEqualTo(600000.0);
        assertThat(orderDb.getStock("P1")).isEqualTo(8);
        assertThat(orderDb.getOrderStatus("CART-01")).isEqualTo(OrderStatus.PAID);
        assertThat(eventPublisher.hasPublished(OrderPlacedEvent.class)).isTrue();
    }

    @Test
    @DisplayName("Từ chối khi giỏ hàng rỗng")
    public void testPlaceOrder_EmptyCart() {
        orderDb.addOrder("CART-01", "CUS-01", OrderStatus.ACTIVE);
        ShippingInfo shipping = new ShippingInfo("Nguyen Van A", "0912345678", "12 Nguyen Hue");

        PlaceOrderModelDTO input = new PlaceOrderModelDTO("CART-01", "CUS-01", new ArrayList<>(),
                shipping, PaymentMethod.COD, null, 0.0);

        PlaceOrderOutputDTO output = placeOrderService.execute(input);

        assertThat(output.isSuccess()).isFalse();
        assertThat(output.getMessage()).isEqualTo("Giỏ hàng không có sản phẩm");
    }
}
