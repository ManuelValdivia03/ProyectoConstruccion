package logic.logicclasses;

public class Student extends User {
    private String enrollment;
    private int grade;

    public Student(int idUser, String fullName, String cellPhone, String phoneExtension,char status ,String enrollment, int grade) {
        super(idUser,fullName,cellPhone,phoneExtension,status);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return getIdUser() == student.getIdUser() &&
                java.util.Objects.equals(getFullName(), student.getFullName()) &&
                java.util.Objects.equals(getCellPhone(), student.getCellPhone()) &&
                java.util.Objects.equals(enrollment, student.enrollment);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getIdUser(), getFullName(), getCellPhone(), enrollment);
    }
}

