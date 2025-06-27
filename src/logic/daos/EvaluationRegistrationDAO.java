package logic.daos;

import dataaccess.ConnectionDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EvaluationRegistrationDAO {

    public boolean isRegistrationEnabled() throws SQLException {
        String sql = "SELECT habilitado FROM evaluaciones_habilitadas WHERE id = 1";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getBoolean("habilitado");
            }
            return false;
        }
    }

    public boolean setRegistrationEnabled(boolean enabled) throws SQLException {
        String sql = "UPDATE evaluaciones_habilitadas SET habilitado = ? WHERE id = 1";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, enabled);
            return statement.executeUpdate() > 0;
        }
    }
}