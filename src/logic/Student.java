package logic;

public class Student extends User{
    private String enrollment;

    public Student(int idUser, String fullName, String cellPhone,char status ,String enrollment) {
        super(idUser,fullName,cellPhone, status);
        this.enrollment = enrollment;
    }

    public Student(){
        super();
        enrollment = "";

    }

    public String getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

}
