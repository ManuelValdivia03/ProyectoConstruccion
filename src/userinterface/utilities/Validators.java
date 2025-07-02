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
        return staffNumber.matches("^\\d{1,5}$");
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

    public boolean validatePhoneExtension(String extension) {
        if (extension == null || extension.isEmpty()) return true;
        return extension.matches("^\\d{1,5}$");
    }

    public boolean validateActivityName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return name.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
    }

    public boolean validateActivityDescription(String description) {
        if (description == null || description.trim().isEmpty()) return false;
        return description.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
    }

    public boolean validateName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return name.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    }

    public boolean validateProjectName(String name) {
        return validateActivityName(name);
    }

    public boolean validateProjectDescription(String description) {
        return validateActivityDescription(description);
    }

    public boolean validateReportTextField(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        return text.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
    }
}
