package logic.logicclasses;

public class LinkedOrganization {
    private int idLinkedOrganization;
    private String nameLinkedOrganization;
    private String cellPhoneLinkedOrganization;
    private String emailLinkedOrganization;
    private char status;

    public LinkedOrganization(int idLinkedOrganization, String nameLinkedOrganization, String cellPhoneLinkedOrganization, String emailLinkedOrganization, char status) {
        this.idLinkedOrganization = idLinkedOrganization;
        this.nameLinkedOrganization = nameLinkedOrganization;
        this.cellPhoneLinkedOrganization = cellPhoneLinkedOrganization;
        this.emailLinkedOrganization = emailLinkedOrganization;
        this.status = status;
    }

    public LinkedOrganization() {
        idLinkedOrganization = 0;
        nameLinkedOrganization = "";
        cellPhoneLinkedOrganization = "";
        emailLinkedOrganization = "";
        status = ' ';
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
}
