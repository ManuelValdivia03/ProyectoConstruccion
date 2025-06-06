package logic.interfaces;

import logic.logicclasses.Academic;
import logic.enums.AcademicType;
import logic.exceptions.RepeatedStaffNumberException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IAcademicDAO {
    boolean addAcademic(Academic academic) throws SQLException, RepeatedStaffNumberException;
    boolean updateAcademic(Academic academic) throws SQLException;
    boolean deleteAcademic(Academic academic) throws SQLException;
    List<Academic> getAllAcademics() throws SQLException;
    Optional<Academic> getAcademicById(int idUser) throws SQLException;
    Optional<Academic> getAcademicByStaffNumber(String staffNumber) throws SQLException;
    List<Academic> getAllAcademicsByType(AcademicType type) throws SQLException;
    boolean changeAcademicType(Academic academic) throws SQLException;
    boolean academicExists(String staffNumber) throws SQLException;
    boolean existsForUser(int userId) throws SQLException;
    int countAcademics() throws SQLException;
    List<Academic> getAllAcademicsFromView() throws SQLException;
    List<Academic> getAcademicsByStatusFromView(char estado) throws SQLException;
    boolean staffNumberExists(String staffNumber) throws RepeatedStaffNumberException, SQLException;
}