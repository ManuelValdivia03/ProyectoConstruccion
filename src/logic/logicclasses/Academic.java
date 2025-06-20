package logic.logicclasses;

import logic.enums.AcademicType;

public class Academic extends User {
    private String staffNumber;
    private AcademicType type;

    public Academic(int idUser,String fullName,String cellPhone,String phoneExtension,char status, String staffNumber,AcademicType type) {
        super(idUser,fullName,cellPhone,phoneExtension,status);
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