package starter;

public class ReviewValidationService {

    public boolean isIntegerRating(double rating) {
        return rating == (int) rating;
    }

    public boolean isValidRatingRange(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public boolean isValidComment(String comment) {
        return comment != null && !comment.trim().isEmpty();
    }
}