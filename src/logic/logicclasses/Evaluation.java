package logic.logicclasses;

import java.sql.Timestamp;

public class Evaluation {
    private int idEvaluation;
    private int calification;
    private String description;
    private Timestamp evaluationDate;
    private Academic academic;
    private Presentation presentation;

    public Evaluation(int idEvaluation, int calification, String description, Timestamp evaluationDate,Academic academic, Presentation presentation) {
        this.idEvaluation = idEvaluation;
        this.calification = calification;
        this.description = description;
        this.evaluationDate = evaluationDate;
        this.academic = academic;
        this.presentation = presentation;
    }

    public Evaluation() {
        idEvaluation = 0;
        calification = 0;
        description = "";
        evaluationDate = null;
        academic = null;
        presentation = null;
    }

    public int getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(int idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public int getCalification() {
        return calification;
    }

    public void setCalification(int calification) {
        this.calification = calification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(Timestamp evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public Academic getAcademic() {
        return academic;
    }

    public void setAcademic(Academic academic) {
        this.academic = academic;
    }

    public Presentation getPresentation() {
        return presentation;
    }

    public void setPresentation(Presentation presentation) {
        this.presentation = presentation;
    }
}
