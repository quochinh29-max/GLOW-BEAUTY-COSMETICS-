package starter;

public class ShippingInfo {
    private String receiverName;
    private String phone;
    private String address;

    public ShippingInfo(String receiverName, String phone, String address) {
        this.receiverName = receiverName;
        this.phone = phone;
        this.address = address;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
