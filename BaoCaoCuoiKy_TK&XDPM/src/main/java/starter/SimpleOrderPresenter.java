package starter;

public class SimpleOrderPresenter implements OrderPresentAble {
    private PlaceOrderOutputDTO lastOutput;

    @Override
    public void format(PlaceOrderOutputDTO output) {
        this.lastOutput = output;
    }

    public PlaceOrderOutputDTO getLastOutput() {
        return lastOutput;
    }
}
