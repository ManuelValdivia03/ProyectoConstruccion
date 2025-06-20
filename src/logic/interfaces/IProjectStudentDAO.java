package logic.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IProjectStudentDAO {
    boolean assignStudentToProject(int projectId, int studentId) throws SQLException;
    boolean removeStudentFromProyect(int proyectId, int studentId) throws SQLException;
    List<Integer> getStudentsByProyect(int proyectId) throws SQLException;
    Integer getProyectByStudent(int studentId) throws SQLException;
}
