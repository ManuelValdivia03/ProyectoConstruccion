package logic.logicclasses;

public class SelfEvaluation {
    private int idSelfEvaluation;
    private String feedBack;
    private float calification;
    private Student student;

    public SelfEvaluation(int idSelfEvaluation, String feedBack, float calification, Student student) {
        this.idSelfEvaluation = idSelfEvaluation;
        this.feedBack = feedBack;
        this.calification = calification;
        this.student = student;
    }

    public SelfEvaluation() {
        idSelfEvaluation = 1;
        feedBack = "";
        calification = 0;
        student = null;
    }

    public int getIdSelfEvaluation() {
        return idSelfEvaluation;
    }

    public void setIdSelfEvaluation(int idSelfEvaluation) {
        this.idSelfEvaluation = idSelfEvaluation;
    }

    public String getFeedBack() {
        return feedBack;
    }

    public void setFeedBack(String feedBack) {
        this.feedBack = feedBack;
    }

    public float getCalification() {
        return calification;
    }

    public void setCalification(float calification) {
        this.calification = calification;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelfEvaluation that = (SelfEvaluation) o;
        return idSelfEvaluation == that.idSelfEvaluation &&
                Float.compare(that.calification, calification) == 0 &&
                java.util.Objects.equals(feedBack, that.feedBack) &&
                java.util.Objects.equals(student, that.student);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idSelfEvaluation, feedBack, calification, student);
    }
}
