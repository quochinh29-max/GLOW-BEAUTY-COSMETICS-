package starter.unittests.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.InMemoryOrderDBMock;
import starter.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryOrderDBMockTest {

    private InMemoryOrderDBMock dbMock;

    @BeforeEach
    public void setUp() {
        dbMock = new InMemoryOrderDBMock();
    }

    @Test
    @DisplayName("Kiểm tra chủ sở hữu giỏ hàng và trạng thái")
    public void testOwnerAndStatus() {
        dbMock.addOrder("CART-01", "CUS-01", OrderStatus.ACTIVE);

        assertThat(dbMock.doesOrderBelongToCustomer("CART-01", "CUS-01")).isTrue();
        assertThat(dbMock.doesOrderBelongToCustomer("CART-01", "CUS-02")).isFalse();
        assertThat(dbMock.getOrderStatus("CART-01")).isEqualTo(OrderStatus.ACTIVE);

        dbMock.updateOrderStatus("CART-01", OrderStatus.PAID);
        assertThat(dbMock.getOrderStatus("CART-01")).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Kiểm tra tồn kho và trừ tồn kho")
    public void testStockOperations() {
        dbMock.addStock("P1", 10);
        assertThat(dbMock.getStock("P1")).isEqualTo(10);

        boolean success = dbMock.deductStock("P1", 3);
        assertThat(success).isTrue();
        assertThat(dbMock.getStock("P1")).isEqualTo(7);

        boolean fail = dbMock.deductStock("P1", 10);
        assertThat(fail).isFalse();
        assertThat(dbMock.getStock("P1")).isEqualTo(7);
    }
}
