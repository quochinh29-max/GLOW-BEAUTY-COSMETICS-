package starter.unittests.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starter.OrderValidationService;
import starter.ShippingInfo;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderValidationServiceTest {

    private OrderValidationService validator;

    @BeforeEach
    public void setUp() {
        validator = new OrderValidationService();
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ của số điện thoại")
    public void testIsValidPhone() {
        assertThat(validator.isValidPhone("0912345678")).isTrue();
        assertThat(validator.isValidPhone("091234567")).isFalse(); // 9 số
        assertThat(validator.isValidPhone("09123456789")).isFalse(); // 11 số
        assertThat(validator.isValidPhone("1912345678")).isFalse(); // Không bắt đầu bằng 0
        assertThat(validator.isValidPhone("09123abc78")).isFalse(); // Chứa ký tự chữ
        assertThat(validator.isValidPhone(null)).isFalse();
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ của số lượng sản phẩm so với tồn kho")
    public void testIsValidQuantity() {
        assertThat(validator.isValidQuantity(1, 10)).isTrue();
        assertThat(validator.isValidQuantity(10, 10)).isTrue();
        assertThat(validator.isValidQuantity(0, 10)).isFalse();
        assertThat(validator.isValidQuantity(-1, 10)).isFalse();
        assertThat(validator.isValidQuantity(11, 10)).isFalse();
    }

    @Test
    @DisplayName("Kiểm tra thông tin giao hàng đầy đủ")
    public void testIsValidShipping() {
        ShippingInfo valid = new ShippingInfo("Nguyen Van A", "0912345678", "12 Nguyen Hue");
        assertThat(validator.isValidShipping(valid)).isTrue();

        ShippingInfo missingName = new ShippingInfo("", "0912345678", "12 Nguyen Hue");
        assertThat(validator.isValidShipping(missingName)).isFalse();

        ShippingInfo invalidPhone = new ShippingInfo("Nguyen Van A", "123", "12 Nguyen Hue");
        assertThat(validator.isValidShipping(invalidPhone)).isFalse();

        ShippingInfo missingAddress = new ShippingInfo("Nguyen Van A", "0912345678", "  ");
        assertThat(validator.isValidShipping(missingAddress)).isFalse();
    }
}
