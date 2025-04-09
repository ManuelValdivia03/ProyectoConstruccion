package logic.interfaces;

import logic.Proyect;

import java.sql.SQLException;
import java.util.List;

public interface IProyectDAO {
    boolean addProyect(Proyect proyect) throws SQLException;
    boolean updateProyect(Proyect proyect) throws SQLException;
    boolean deleteProyect(Proyect proyect) throws SQLException;
    List<Proyect> getAllProyects() throws SQLException;
    Proyect getProyectById(int id) throws SQLException;
    Proyect getProyectByTitle(String title) throws SQLException;
    int countProyects() throws SQLException;
    boolean proyectExists(String title) throws SQLException;
}
