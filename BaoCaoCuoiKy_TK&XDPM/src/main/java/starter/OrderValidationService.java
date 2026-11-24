package starter;

public class OrderValidationService {

    public boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        // Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0
        return phone.matches("^0\\d{9}$");
    }

    public boolean isValidQuantity(int quantity, int currentStock) {
        // Số lượng phải nguyên dương và không vượt quá tồn kho
        return quantity > 0 && quantity <= currentStock;
    }

    public boolean isValidShipping(ShippingInfo shippingInfo) {
        if (shippingInfo == null) {
            return false;
        }
        if (shippingInfo.getReceiverName() == null || shippingInfo.getReceiverName().trim().isEmpty()) {
            return false;
        }
        if (!isValidPhone(shippingInfo.getPhone())) {
            return false;
        }
        if (shippingInfo.getAddress() == null || shippingInfo.getAddress().trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
