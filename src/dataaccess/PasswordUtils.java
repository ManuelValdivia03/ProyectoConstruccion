package dataaccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final Logger logger = LogManager.getLogger(PasswordUtils.class);
    private static final int BCRYPT_COST = 12;

    public static String hashPassword(String plainPassword) {
        logger.debug("Generando hash para una contraseña");
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
        logger.info("Hash generado correctamente");
        return hash;
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            boolean match = BCrypt.checkpw(plainPassword, hashedPassword);
            logger.debug("Comparación de contraseña: {}", match ? "coinciden" : "no coinciden");
            return match;
        } catch (Exception e) {
            logger.error("Error al comparar contraseñas", e);
            return false;
        }
    }
}
