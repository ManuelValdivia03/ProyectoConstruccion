package logic.exceptions;

public class InvalidCellPhoneException extends RuntimeException {
    public InvalidCellPhoneException(String message) {
        super(message);
    }
    public InvalidCellPhoneException() {
        super();
    }
}
