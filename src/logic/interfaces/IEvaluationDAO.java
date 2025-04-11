package logic.interfaces;

import logic.Evaluation;
import java.sql.SQLException;
import java.util.List;

public interface IEvaluationDAO {
    boolean addEvaluation(Evaluation evaluation) throws SQLException;
    Evaluation getEvaluationById(int idEvaluation) throws SQLException;
    List<Evaluation> getAllEvaluations() throws SQLException;
    List<Evaluation> getEvaluationsByAcademic(int academicId) throws SQLException;
    List<Evaluation> getEvaluationsByPresentation(int presentationId) throws SQLException;
    boolean updateEvaluation(Evaluation evaluation) throws SQLException;
    boolean deleteEvaluation(int idEvaluation) throws SQLException;
    boolean evaluationExists(int idEvaluation) throws SQLException;
    int countEvaluations() throws SQLException;
}