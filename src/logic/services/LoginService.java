package logic.services;

import dataaccess.PasswordUtils;
import logic.daos.*;
import logic.logicclasses.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class LoginService {
    private static final Logger logger = LogManager.getLogger(LoginService.class);

    private final AccountDAO accountDAO;
    private final CoordinatorDAO coordinatorDAO;
    private final AcademicDAO academicDAO;
    private final StudentDAO studentDAO;

    public LoginService(AccountDAO accountDAO, CoordinatorDAO coordinatorDAO,
                        AcademicDAO academicDAO, StudentDAO studentDAO) {
        this.accountDAO = accountDAO;
        this.coordinatorDAO = coordinatorDAO;
        this.academicDAO = academicDAO;
        this.studentDAO = studentDAO;
    }

    public User login(String email, String password) {
        try {
            if (!accountDAO.verifyCredentials(email, password)) {
                logger.warn("Credenciales inválidas para: {}", email);
                return null;
            }

            Account cuenta = accountDAO.getAccountByEmail(email);
            if (cuenta == null) {
                logger.error("Cuenta no encontrada para email: {}", email);
                return null;
            }

            if (coordinatorDAO.existsForUser(cuenta.getIdUser())) {
                logger.debug("Usuario identificado como coordinador");
                return coordinatorDAO.getFullCoordinator(cuenta.getIdUser());
            }
            else if (academicDAO.existsForUser(cuenta.getIdUser())) {
                logger.debug("Usuario identificado como académico");
                return academicDAO.getFullAcademic(cuenta.getIdUser());
            }
            else if (studentDAO.existsForUser(cuenta.getIdUser())) {
                logger.debug("Usuario identificado como estudiante");
                return studentDAO.getFullStudent(cuenta.getIdUser());
            }

            logger.warn("Usuario no tiene tipo asignado: {}", email);
            return null;

        } catch (SQLException e) {
            logger.error("Error durante login para: " + email, e);
            return null;
        }
    }
}