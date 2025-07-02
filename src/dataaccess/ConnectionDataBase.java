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
            throw new SQLException("Database configuration not available");
        }

        String url = ConfigLoader.get("db.url");
        String user = ConfigLoader.get("db.user");
        String password = ConfigLoader.get("db.password");

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            String userMessage;
            switch (e.getSQLState()) {
                case "28000":
                    userMessage = "Acceso denegado: usuario o contraseña invalida.";
                    break;
                case "42000":
                    if (e.getErrorCode() == 1049) {
                        userMessage = "No existe la base de datos.";
                    } else if (e.getErrorCode() == 1044) {
                        userMessage = "Acceso denegado a la base de datos para el usuario.";
                    } else if (e.getErrorCode() == 1146) {
                        userMessage = "No existe la tabla.";
                    } else {
                        userMessage = "SQL syntax error o violación de permisos.";
                    }
                    break;
                case "42S02":
                    userMessage = "Tabla no existe.";
                    break;
                default:
                    userMessage = "Error de base de datos: " + e.getMessage();
            }
            logger.error("Connection error to {} - Code: {} - State: {} - Message: {}",
                    url, e.getErrorCode(), e.getSQLState(), e.getMessage());
            throw new SQLException(userMessage, e);
        }
    }
}
