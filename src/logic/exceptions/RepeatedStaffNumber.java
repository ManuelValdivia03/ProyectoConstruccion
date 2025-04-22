package logic.exceptions;

public class RepeatedStaffNumber extends RuntimeException {
    public RepeatedStaffNumber(String message) {
        super("Este numero de personal ya esta registrado");
    }

    public RepeatedStaffNumber() {

    }
}
