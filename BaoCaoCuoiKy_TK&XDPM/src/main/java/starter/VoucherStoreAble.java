package starter;

public interface VoucherStoreAble {
    Voucher findVoucher(String code);
    boolean isVoucherApplied(String orderId, String code);
    boolean saveAppliedVoucher(String orderId, String code, double discountAmount);
}