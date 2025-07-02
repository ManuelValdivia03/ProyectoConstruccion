package dataaccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import logic.services.ExceptionManager;

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
            String errorMessage = ExceptionManager.handleException(e);
            logger.error(errorMessage);
            throw e;
        }
    }
}