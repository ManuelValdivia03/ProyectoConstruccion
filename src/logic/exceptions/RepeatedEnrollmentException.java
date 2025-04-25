package logic.exceptions;

public class RepeatedEnrollmentException extends RuntimeException {
    public RepeatedEnrollmentException(String message) {
        super(message);
    }
    public RepeatedEnrollmentException() {
    }
}
