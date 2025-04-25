package userinterface.utilities;

public class Validators {


    public static boolean validateEmail(String email) {
        if (email == null) return false;
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }


    public static boolean validateCellPhone(String cellPhone) {
        if (cellPhone == null) return false;
        return cellPhone.matches("^\\d{10}$");
    }

    public static boolean validateStaffNumber(String staffNumber) {
        if (staffNumber == null) return false;
        return staffNumber.matches("^\\d{5}$");
    }

    public static boolean validateEnrollment(String enrollment) {
        if (enrollment == null) return false;
        return enrollment.matches("^S\\d{8}$");
    }

    public static boolean validatePassword(String password) {
        if (password == null) return false;
        return password.length() >= 8;
    }
}