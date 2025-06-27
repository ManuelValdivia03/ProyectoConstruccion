package logic.logicclasses;

import logic.enums.AcademicType;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Academic academic = (Academic) o;
        return Objects.equals(staffNumber, academic.staffNumber) &&
                Objects.equals(getFullName(), academic.getFullName()) &&
                Objects.equals(getCellPhone(), academic.getCellPhone()) &&
                Objects.equals(type, academic.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffNumber, getFullName(), getCellPhone(), type);
    }

}