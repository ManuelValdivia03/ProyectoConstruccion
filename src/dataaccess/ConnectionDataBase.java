package dataaccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDataBase {
    private static final Logger logger = LogManager.getLogger(ConnectionDataBase.class);

    public static Connection getConnection() throws SQLException {
        if (ConfigLoader.get("db.url") == null) {
            logger.fatal("Configuración de base de datos no encontrada");
            throw new SQLException("Configuración de DB no disponible");
        }

        String url = ConfigLoader.get("db.url");
        String user = ConfigLoader.get("db.user");
        String password = ConfigLoader.get("db.password");

        logger.debug("Iniciando conexión a DB - URL: {}", url);
        logger.trace("Credenciales - Usuario: {}", user);

        long startTime = System.currentTimeMillis();

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            long connectionTime = System.currentTimeMillis() - startTime;

            logger.info("Conexión exitosa a {} [{} ms]",
                    url.replaceAll("\\?.*", ""), // Limpia parámetros de URL
                    connectionTime);

            if (logger.isDebugEnabled()) {
                logger.debug("Propiedades de conexión: AutoCommit={}, Isolation={}, ReadOnly={}",
                        conn.getAutoCommit(),
                        conn.getTransactionIsolation(),
                        conn.isReadOnly());
            }

            return conn;
        } catch (SQLException e) {
            logger.error("Error de conexión a {} - Código: {} - Estado: {} - Mensaje: {}",
                    url,
                    e.getErrorCode(),
                    e.getSQLState(),
                    e.getMessage());

            logger.debug("Stack trace completo:", e);
            throw e;
        }
    }
}