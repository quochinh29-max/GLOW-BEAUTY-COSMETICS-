package starter;

public class SimpleVoucherPresenter implements VoucherPresentAble {
    private ApplyVoucherOutputDTO lastOutput;

    @Override
    public void format(ApplyVoucherOutputDTO outputDTO) {
        this.lastOutput = outputDTO;
    }

    public ApplyVoucherOutputDTO getLastOutput() {
        return lastOutput;
    }
}