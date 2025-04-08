package logic;

import logic.enumacademic.AcademicType;

public class Academic extends User{
    String staffNumber;
    AcademicType type;

    public Academic(int idUser,String fullName,String cellPhone,String staffNumber, AcademicType type) {
        super(idUser,fullName,cellPhone);
        this.staffNumber = staffNumber;
        this.type = type;
    }

    public Academic() {
        super();
        staffNumber = "";
        type = AcademicType.NONE;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        this.staffNumber = staffNumber;
    }

    public AcademicType getAcademicType() {
        return type;
    }

    public void setAcademicType(AcademicType type) {
        this.type = type;
    }
}