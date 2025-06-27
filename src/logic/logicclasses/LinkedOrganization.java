package logic.logicclasses;

public class LinkedOrganization {
    private int idLinkedOrganization;
    private String nameLinkedOrganization;
    private String cellPhoneLinkedOrganization;
    private String phoneExtension;
    private String department;
    private String emailLinkedOrganization;
    private char status;

    public LinkedOrganization(int idLinkedOrganization, String nameLinkedOrganization, 
            String cellPhoneLinkedOrganization, String phoneExtension, String department, 
            String emailLinkedOrganization, char status) {
        this.idLinkedOrganization = idLinkedOrganization;
        this.nameLinkedOrganization = nameLinkedOrganization;
        this.cellPhoneLinkedOrganization = cellPhoneLinkedOrganization;
        this.phoneExtension = phoneExtension;
        this.department = department;
        this.emailLinkedOrganization = emailLinkedOrganization;
        this.status = status;
    }

    public LinkedOrganization() {
        idLinkedOrganization = 0;
        nameLinkedOrganization = "";
        cellPhoneLinkedOrganization = "";
        phoneExtension = "";
        department = "";
        emailLinkedOrganization = "";
        status = 'A';
    }

    public int getIdLinkedOrganization() {
        return idLinkedOrganization;
    }

    public void setIdLinkedOrganization(int idLinkedOrganization) {
        this.idLinkedOrganization = idLinkedOrganization;
    }

    public String getNameLinkedOrganization() {
        return nameLinkedOrganization;
    }

    public void setNameLinkedOrganization(String nameLinkedOrganization) {
        this.nameLinkedOrganization = nameLinkedOrganization;
    }

    public String getCellPhoneLinkedOrganization() {
        return cellPhoneLinkedOrganization;
    }

    public void setCellPhoneLinkedOrganization(String cellPhoneLinkedOrganization) {
        this.cellPhoneLinkedOrganization = cellPhoneLinkedOrganization;
    }

    public String getEmailLinkedOrganization() {
        return emailLinkedOrganization;
    }

    public void setEmailLinkedOrganization(String emailLinkedOrganization) {
        this.emailLinkedOrganization = emailLinkedOrganization;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkedOrganization that = (LinkedOrganization) o;
        return idLinkedOrganization == that.idLinkedOrganization &&
                status == that.status &&
                java.util.Objects.equals(nameLinkedOrganization, that.nameLinkedOrganization) &&
                java.util.Objects.equals(cellPhoneLinkedOrganization, that.cellPhoneLinkedOrganization) &&
                java.util.Objects.equals(phoneExtension, that.phoneExtension) &&
                java.util.Objects.equals(department, that.department) &&
                java.util.Objects.equals(emailLinkedOrganization, that.emailLinkedOrganization);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idLinkedOrganization, nameLinkedOrganization, cellPhoneLinkedOrganization, phoneExtension, department, emailLinkedOrganization, status);
    }
}
