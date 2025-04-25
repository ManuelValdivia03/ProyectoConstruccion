package logic.interfaces;

import logic.logicclasses.Student;
import java.sql.SQLException;
import java.util.List;

public interface IStudentDAO {
    boolean addStudent(Student student) throws SQLException;
    List<Student> getAllStudents() throws SQLException;
    Student getStudentById(int id) throws SQLException;
    boolean updateStudent(Student student) throws SQLException;
    boolean deleteStudent(int id) throws SQLException;
    List<Student> getStudentsByGroup(int nrc) throws SQLException;
    boolean studentExistsById(int id) throws SQLException;
    int countStudents() throws SQLException;
}