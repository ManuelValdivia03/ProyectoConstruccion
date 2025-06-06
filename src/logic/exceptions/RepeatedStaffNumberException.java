package logic.exceptions;

public class RepeatedStaffNumberException extends RuntimeException {
    public RepeatedStaffNumberException(String message) {
        super("Este numero de personal ya esta registrado");
    }

    public RepeatedStaffNumberException() {

    }

    public RepeatedStaffNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
