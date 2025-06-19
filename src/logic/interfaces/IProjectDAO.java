package logic.interfaces;

import logic.logicclasses.Project;
import logic.logicclasses.User;
import java.sql.SQLException;
import java.util.List;

public interface IProjectDAO {
    boolean addProyect(Project project) throws SQLException;
    boolean updateProyect(Project project) throws SQLException;
    boolean deleteProyect(Project project) throws SQLException;
    List<Project> getAllProyects() throws SQLException;
    List<Project> getProyectsByStatus(char status) throws SQLException;
    Project getProyectById(int id) throws SQLException;
    Project getProyectByTitle(String title) throws SQLException;
    int countProyects() throws SQLException;
    boolean proyectExists(String title) throws SQLException;
    boolean changeProyectStatus(Project project) throws SQLException;
    int addProyectAndGetId(Project project) throws SQLException;
    boolean linkProjectToRepresentative(int projectId, int representativeId) throws SQLException;
    List<Project> getAvailableProjects() throws SQLException;
    boolean incrementStudentCount(int projectId) throws SQLException;
}
