package starter;

public class SimpleReviewPresenter implements ReviewPresentAble {
    private SubmitReviewOutputDTO lastOutput;

    @Override
    public void format(SubmitReviewOutputDTO outputDTO) {
        this.lastOutput = outputDTO;
    }

    public SubmitReviewOutputDTO getLastOutput() {
        return lastOutput;
    }
}