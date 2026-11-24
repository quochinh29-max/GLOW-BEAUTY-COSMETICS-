package starter.unittests.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.OrderPricingService;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderPricingServiceTest {

    private OrderPricingService pricingService;

    @BeforeEach
    public void setUp() {
        pricingService = new OrderPricingService();
    }

    @Test
    @DisplayName("Miễn phí vận chuyển khi tạm tính >= 500000 VND")
    public void testShippingFee_FreeShippingWhenEligible() {
        assertThat(pricingService.calculateShippingFee(500000.0)).isEqualTo(0.0);
        assertThat(pricingService.calculateShippingFee(600000.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Tính phí 30000 VND khi tạm tính < 500000 VND")
    public void testShippingFee_ChargedWhenUnder500k() {
        assertThat(pricingService.calculateShippingFee(499999.0)).isEqualTo(30000.0);
        assertThat(pricingService.calculateShippingFee(100000.0)).isEqualTo(30000.0);
    }

    @Test
    @DisplayName("Tổng thanh toán bằng tạm tính trừ giảm giá cộng phí ship")
    public void testCalculateTotalPayable() {
        double total = pricingService.calculateTotalPayable(600000.0, 50000.0, 0.0);
        assertThat(total).isEqualTo(550000.0);

        double totalWithShipping = pricingService.calculateTotalPayable(400000.0, 40000.0, 30000.0);
        assertThat(totalWithShipping).isEqualTo(390000.0);
    }
}
