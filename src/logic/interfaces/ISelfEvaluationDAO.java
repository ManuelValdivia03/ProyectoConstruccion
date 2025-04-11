package logic.interfaces;

import logic.SelfEvaluation;
import java.sql.SQLException;
import java.util.List;

public interface ISelfEvaluationDAO {
    boolean addSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException;
    SelfEvaluation getSelfEvaluationById(int idSelfEvaluation) throws SQLException;
    List<SelfEvaluation> getAllSelfEvaluations() throws SQLException;
    List<SelfEvaluation> getSelfEvaluationsByStudent(int studentId) throws SQLException;
    boolean updateSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException;
    boolean deleteSelfEvaluation(int idSelfEvaluation) throws SQLException;
    boolean selfEvaluationExists(int idSelfEvaluation) throws SQLException;
    int countSelfEvaluations() throws SQLException;
}