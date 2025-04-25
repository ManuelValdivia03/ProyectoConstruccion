package logic.exceptions;

public class RepeatedCellPhoneException extends RuntimeException {
    public RepeatedCellPhoneException(String message) {
        super(message);
    }
    public RepeatedCellPhoneException() {
        super();
    }
}
