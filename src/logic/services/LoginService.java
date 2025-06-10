package logic.services;

import logic.daos.AccountDAO;
import logic.daos.AcademicDAO;
import logic.daos.CoordinatorDAO;
import logic.daos.StudentDAO;
import logic.exceptions.InvalidCredentialsException;
import logic.logicclasses.Account;
import logic.logicclasses.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;
import java.util.Objects;

public class LoginService {
    private static final Logger logger = LogManager.getLogger(LoginService.class);

    private final AccountDAO accountDAO;
    private final CoordinatorDAO coordinatorDAO;
    private final AcademicDAO academicDAO;
    private final StudentDAO studentDAO;

    public LoginService(AccountDAO accountDAO, CoordinatorDAO coordinatorDAO,
                        AcademicDAO academicDAO, StudentDAO studentDAO) {
        this.accountDAO = Objects.requireNonNull(accountDAO, "La cuenta no puede ser nula");
        this.coordinatorDAO = Objects.requireNonNull(coordinatorDAO, "El coordinador no puede ser nulo");
        this.academicDAO = Objects.requireNonNull(academicDAO, "El academico no puede ser nulo");
        this.studentDAO = Objects.requireNonNull(studentDAO, "El estudiante no puede ser nulo");
    }

    public User login(String email, String password) throws InvalidCredentialsException, SQLException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            logger.warn("Email o contraseña vacíos");
            throw new InvalidCredentialsException("Email o contraseña vacíos");
        }

        if (!accountDAO.verifyCredentials(email, password)) {
            logger.warn("Credenciales inválidas para: {}", email);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        Account cuenta = accountDAO.getAccountByEmail(email);
        if (cuenta == null) {
            logger.error("Cuenta no encontrada para email: {}", email);
            throw new SQLException("Cuenta no encontrada en la base de datos");
        }

        int userId = cuenta.getIdUser();
        if (coordinatorDAO.existsForUser(userId)) {
            logger.debug("Usuario identificado como coordinador");
            return coordinatorDAO.getFullCoordinator(userId);
        }
        if (academicDAO.existsAcademic(userId)) {
            logger.debug("Usuario identificado como académico");
            return academicDAO.getAcademicById(userId);
        }
        if (studentDAO.existsForUser(userId)) {
            logger.debug("Usuario identificado como estudiante");
            return studentDAO.getFullStudent(userId);
        }

        // Este caso teóricamente nunca debería ocurrir según los requisitos
        logger.error("Usuario sin tipo asignado para: {}", email);
        throw new IllegalStateException("Estado inválido: usuario sin tipo asignado");
    }
}

