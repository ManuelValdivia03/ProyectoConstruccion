package logic.exceptions;

public class RepeatedEmailException extends RuntimeException {
    public RepeatedEmailException(String message) {
        super(message);
    }
    public RepeatedEmailException() {
        super();
    }
}
