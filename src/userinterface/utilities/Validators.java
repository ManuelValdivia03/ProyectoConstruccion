package userinterface.utilities;

public class Validators {


    public boolean validateEmail(String email) {
        if (email == null) return false;
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }


    public boolean validateCellPhone(String cellPhone) {
        if (cellPhone == null) return false;
        return cellPhone.matches("^\\d{10}$");
    }

    public boolean validateStaffNumber(String staffNumber) {
        if (staffNumber == null) return false;
        return staffNumber.matches("^\\d{5}$");
    }

    public boolean validateEnrollment(String enrollment) {
        if (enrollment == null) return false;
        return enrollment.matches("^[Ss]\\d{8}$");
    }

    public boolean validatePassword(String password) {
        if (password == null) return false;
        return password.length() >= 8;
    }

    public boolean validateDate(String date) {
        if (date == null) return false;
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$") ||
                date.matches("^\\d{2}/\\d{2}/\\d{4}$") ||
                date.matches("^\\d{2}-\\d{2}-\\d{4}$");
    }
}