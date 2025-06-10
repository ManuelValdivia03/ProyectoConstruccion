package logic.services;

import logic.daos.AccountDAO;
import logic.daos.AcademicDAO;
import logic.daos.CoordinatorDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Account;
import logic.logicclasses.Academic;
import logic.logicclasses.Coordinator;
import logic.logicclasses.Student;
import dataaccess.PasswordUtils;
import logic.exceptions.UserNotFoundException;
import java.sql.SQLException;

public class PasswordRecoveryService {
    private final AccountDAO accountDAO;
    private final CoordinatorDAO coordinatorDAO;
    private final AcademicDAO academicDAO;
    private final StudentDAO studentDAO;

    public PasswordRecoveryService(AccountDAO accountDAO, CoordinatorDAO coordinatorDAO,
                                   AcademicDAO academicDAO, StudentDAO studentDAO) {
        this.accountDAO = accountDAO;
        this.coordinatorDAO = coordinatorDAO;
        this.academicDAO = academicDAO;
        this.studentDAO = studentDAO;
    }

    public boolean validateUser(String email, String identifier) throws UserNotFoundException, SQLException {
        if (!accountDAO.accountExists(email)) {
            throw new UserNotFoundException("No se encontró una cuenta con ese correo electrónico");
        }

        Account account = accountDAO.getAccountByEmail(email);
        int userId = account.getIdUser();

        if (coordinatorDAO.existsForUser(userId)) {
            Coordinator coord = coordinatorDAO.getFullCoordinator(userId);
            if (!coord.getStaffNumber().equals(identifier)) {
                throw new UserNotFoundException("Número de personal no coincide");
            }
        }
        else if (academicDAO.existsAcademic(userId)) {
            Academic acad = academicDAO.getAcademicById(userId);
            if (!acad.getStaffNumber().equals(identifier)) {
                throw new UserNotFoundException("Número de personal no coincide");
            }
        }
        else if (studentDAO.existsForUser(userId)) {
            Student student = studentDAO.getFullStudent(userId);
            if (!student.getEnrollment().equals(identifier)) {
                throw new UserNotFoundException("Matrícula no coincide");
            }
        }
        else {
            throw new UserNotFoundException("Usuario no reconocido");
        }

        return true;
    }

    public void updatePassword(String email, String newPassword) throws SQLException {
        String hashedPassword = PasswordUtils.hashPassword(newPassword);

        boolean updated = accountDAO.updatePasswordByEmail(email, hashedPassword);

        if (!updated) {
            throw new SQLException("No se pudo actualizar la contraseña");
        }

        Account updatedAccount = accountDAO.getAccountByEmail(email);
    }
}
