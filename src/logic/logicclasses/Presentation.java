package logic.logicclasses;

import logic.enums.PresentationType;

import java.sql.Timestamp;

public class Presentation {
    private int idPresentation;
    private Timestamp PresentationDate;
    private PresentationType PresentationType;
    private Student student;

    public Presentation(int idPresentation, PresentationType PresentationType, Timestamp PresentationDate, Student student) {
        this.idPresentation = idPresentation;
        this.PresentationType = PresentationType;
        this.PresentationDate = PresentationDate;
        this.student = student;
    }

    public Presentation() {
        idPresentation = 0;
        PresentationDate = null;
        PresentationType = null;
        student = null;
    }

    public int getIdPresentation() {
        return idPresentation;
    }

    public void setIdPresentation(int idPresentation) {
        this.idPresentation = idPresentation;
    }

    public Timestamp getPresentationDate() {
        return PresentationDate;
    }

    public void setPresentationDate(Timestamp PresentationDate) {
        this.PresentationDate = PresentationDate;
    }

    public PresentationType getPresentationType() {
        return PresentationType;
    }

    public void setPresentationType(PresentationType PresentationType) {
        this.PresentationType = PresentationType;
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
        Presentation that = (Presentation) o;
        return idPresentation == that.idPresentation &&
                java.util.Objects.equals(PresentationDate, that.PresentationDate) &&
                PresentationType == that.PresentationType &&
                java.util.Objects.equals(student, that.student);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idPresentation, PresentationDate, PresentationType, student);
    }
}
