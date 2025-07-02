package logic.services;

import logic.daos.*;
import logic.exceptions.*;
import logic.logicclasses.LinkedOrganization;

import java.sql.SQLException;

public class DataVerificationService {
    public static boolean verifyAcademicDataUniqueness(String phone, String staffNumber, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedStaffNumberException, RepeatedEmailException {
        UserDAO userDAO = new UserDAO();
        AcademicDAO academicDAO = new AcademicDAO();
        AccountDAO accountDAO = new AccountDAO();

        if (userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (academicDAO.academicExists(staffNumber)) {
            throw new RepeatedStaffNumberException();
        }
        if (accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
        }
        return true;
    }

    public static boolean verifyStudentDataUniqueness(String phone, String enrollment, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedEnrollmentException, RepeatedEmailException {
        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();
        AccountDAO accountDAO = new AccountDAO();

        if (userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (studentDAO.enrollmentExists(enrollment)) {
            throw new RepeatedEnrollmentException();
        }
        if (accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
        }
        return true;
    }

    public static void verifyLinkedOrganizationDataUniqueness(String name, String phone, String email)
            throws SQLException, RepeatedNameLinkedOrganizationException, RepeatedCellPhoneException, RepeatedEmailException {
        LinkedOrganizationDAO dao = new LinkedOrganizationDAO();
        if (dao.linkedOrganizationExists(name)) {
            throw new RepeatedNameLinkedOrganizationException("La organización ya está registrada");
        }
        if (dao.phoneNumberExists(phone)) {
            throw new RepeatedCellPhoneException("El número de teléfono ya está registrado");
        }
        if (dao.emailExists(email)) {
            throw new RepeatedEmailException("El correo electrónico ya está registrado");
        }
    }

    public static void verifyAcademicUpdateUniqueness(String phone, String email, String originalPhone, String originalEmail)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {
        UserDAO userDAO = new UserDAO();
        AccountDAO accountDAO = new AccountDAO();

        if (!phone.equals(originalPhone) && userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (!email.equals(originalEmail) && accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
        }
    }

    public static void verifyStudentUpdateUniqueness(String phone, String email, String originalPhone, String originalEmail)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {
        UserDAO userDAO = new UserDAO();
        AccountDAO accountDAO = new AccountDAO();

        if (!phone.equals(originalPhone) && userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (!email.equals(originalEmail) && accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
        }
    }

    public static void verifyLinkedOrganizationUpdateUniqueness(LinkedOrganization org, LinkedOrganization originalOrg)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {
        LinkedOrganizationDAO dao = new LinkedOrganizationDAO();
        if (!org.getCellPhoneLinkedOrganization().equals(originalOrg.getCellPhoneLinkedOrganization())
                && dao.phoneNumberExists(org.getCellPhoneLinkedOrganization())) {
            throw new RepeatedCellPhoneException("Teléfono duplicado");
        }
        if (!org.getEmailLinkedOrganization().equals(originalOrg.getEmailLinkedOrganization())
                && dao.emailExists(org.getEmailLinkedOrganization())) {
            throw new RepeatedEmailException("Email duplicado");
        }
    }
}

