package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Proyect;
import logic.interfaces.IProyectDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProyectDAO implements IProyectDAO {

    public boolean addProyect(Proyect proyect) throws SQLException {
        String sql = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, proyect.getTitle());
            ps.setString(2, proyect.getDescription());
            ps.setTimestamp(3, proyect.getDateStart());
            ps.setTimestamp(4, proyect.getDateEnd());
            ps.setString(5, String.valueOf(proyect.getStatus()));

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        proyect.setIdProyect(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean updateProyect(Proyect proyect) throws SQLException {
        String sql = "UPDATE proyecto SET titulo = ?, descripcion = ?, fecha_inicial = ?, fecha_terminal = ?, estado = ? WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, proyect.getTitle());
            ps.setString(2, proyect.getDescription());
            ps.setTimestamp(3, proyect.getDateStart());
            ps.setTimestamp(4, proyect.getDateEnd());
            ps.setString(5, String.valueOf(proyect.getStatus()));
            ps.setInt(6, proyect.getIdProyect());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteProyect(Proyect proyect) throws SQLException {
        String sql = "DELETE FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, proyect.getIdProyect());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Proyect> getAllProyects() throws SQLException {
        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto";
        List<Proyect> proyects = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Proyect proyect = new Proyect(
                        rs.getInt("id_proyecto"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getTimestamp("fecha_inicial"),
                        rs.getTimestamp("fecha_terminal"),
                        rs.getString("estado").charAt(0)
                );
                proyects.add(proyect);
            }
        }
        return proyects;
    }

    public Proyect getProyectById(int id) throws SQLException {
        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Proyect(
                            rs.getInt("id_proyecto"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getTimestamp("fecha_inicial"),
                            rs.getTimestamp("fecha_terminal"),
                            rs.getString("estado").charAt(0)
                    );
                }
            }
        }
        return null;
    }

    public Proyect getProyectByTitle(String title) throws SQLException {
        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE titulo = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Proyect(
                            rs.getInt("id_proyecto"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getTimestamp("fecha_inicial"),
                            rs.getTimestamp("fecha_terminal"),
                            rs.getString("estado").charAt(0)
                    );
                }
            }
        }
        return null;
    }

    public int countProyects() throws SQLException {
        String sql = "SELECT COUNT(*) FROM proyecto";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public boolean proyectExists(String title) throws SQLException {
        String sql = "SELECT 1 FROM proyecto WHERE titulo = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean changeProyectStatus(Proyect proyect) throws SQLException {
        String sql = "UPDATE proyecto SET estado = ? WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(proyect.getStatus()));
            ps.setInt(2, proyect.getIdProyect());

            return ps.executeUpdate() > 0;
        }
    }
}