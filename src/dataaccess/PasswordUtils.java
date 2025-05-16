package dataaccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final Logger logger = LogManager.getLogger(PasswordUtils.class);
    private static final int BCRYPT_COST = 12;

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        logger.debug("Generando hash para contraseña");
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty() ||
                hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }

        try {
            boolean match = BCrypt.checkpw(plainPassword, hashedPassword);
            logger.debug("Resultado comparación contraseña: {}", match ? "VÁLIDA" : "INVÁLIDA");
            return match;
        } catch (Exception e) {
            logger.error("Error al verificar contraseña", e);
            return false;
        }
    }
}
