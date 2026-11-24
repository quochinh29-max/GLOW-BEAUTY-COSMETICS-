package starter;

public class SubmitReviewOutputDTO {
    private boolean success;
    private String message;
    private boolean saved;
    private boolean eventPublished;

    public SubmitReviewOutputDTO(boolean success, String message, boolean saved, boolean eventPublished) {
        this.success = success;
        this.message = message;
        this.saved = saved;
        this.eventPublished = eventPublished;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public boolean isSaved() { return saved; }
    public boolean isEventPublished() { return eventPublished; }
}