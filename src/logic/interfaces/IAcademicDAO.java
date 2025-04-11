package logic.interfaces;

import logic.Academic;
import logic.enums.AcademicType;

import java.sql.SQLException;
import java.util.List;

public interface IAcademicDAO {
    boolean addAcademic(Academic academic) throws SQLException;
    boolean deleteAcademic(Academic academic) throws SQLException;
    boolean updateAcademic(Academic academic) throws SQLException;
    Academic getAcademicByStaffNumber(String staffNumber) throws SQLException;
    List<Academic> getAllAcademics() throws SQLException;
    List<Academic> getAllAcademicsByType(AcademicType type) throws SQLException;
    boolean changeAcademicType(Academic academic) throws SQLException;
    boolean academicExists(String staffNumber) throws SQLException;
    int countAcademics() throws SQLException;
    boolean staffNumberExists(String staffNumber) throws SQLException;
    Academic getAcademicById(int idUser) throws SQLException;
}
