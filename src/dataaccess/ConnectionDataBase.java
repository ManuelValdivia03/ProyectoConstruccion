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
            throw new SQLException("Configuración de DB no disponible");
        }

        String url = ConfigLoader.get("db.url");
        String user = ConfigLoader.get("db.user");
        String password = ConfigLoader.get("db.password");

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (SQLException e) {
            logger.error("Error de conexión a {} - Código: {} - Estado: {} - Mensaje: {}",
                    url,
                    e.getErrorCode(),
                    e.getSQLState(),
                    e.getMessage());
            throw e;
        }
    }
}
