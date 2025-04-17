package logic.interfaces;

import logic.logicclasses.Presentation;
import java.sql.SQLException;
import java.util.List;

public interface IPresentationDAO {
    boolean addPresentation(Presentation presentation) throws SQLException;
    Presentation getPresentationById(int idPresentation) throws SQLException;
    List<Presentation> getAllPresentations() throws SQLException;
    List<Presentation> getPresentationsByStudent(int studentId) throws SQLException;
    List<Presentation> getPresentationsByType(String presentationType) throws SQLException;
    boolean updatePresentation(Presentation presentation) throws SQLException;
    boolean deletePresentation(int idPresentation) throws SQLException;
    boolean presentationExists(int idPresentation) throws SQLException;
    int countPresentations() throws SQLException;
}