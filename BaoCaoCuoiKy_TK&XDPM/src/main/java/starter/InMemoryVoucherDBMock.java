package starter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryVoucherDBMock implements VoucherStoreAble {
    private final Map<String, Voucher> vouchers = new HashMap<>();
    private final List<String> appliedRecords = new ArrayList<>();
    private boolean simulateError = false;

    public void addVoucher(Voucher voucher) {
        if (voucher != null) {
            vouchers.put(voucher.getCode(), voucher);
        }
    }

    public void setSimulateError(boolean simulateError) {
        this.simulateError = simulateError;
    }

    @Override
    public Voucher findVoucher(String code) {
        return vouchers.get(code);
    }

    @Override
    public boolean isVoucherApplied(String orderId, String code) {
        return appliedRecords.contains(orderId + "#" + code);
    }

    @Override
    public boolean saveAppliedVoucher(String orderId, String code, double discountAmount) {
        if (simulateError) {
            return false;
        }
        appliedRecords.add(orderId + "#" + code);
        return true;
    }
}