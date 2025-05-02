package logic.logicclasses;

public class Student extends User {
    private String enrollment;
    private int grade;

    public Student(int idUser, String fullName, String cellPhone,char status ,String enrollment, int grade) {
        super(idUser,fullName,cellPhone, status);
        this.enrollment = enrollment;
        this.grade = grade;
    }

    public Student(){
        super();
        enrollment = "";
        grade = 0;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

}
