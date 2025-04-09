package logic;

public class LinkedOrganization {
    private int idLinkedOrganization;
    private String nameLinkedOrganization;
    private String cellPhoneLinkedOrganization;
    private String emailLinkedOrganization;

    public LinkedOrganization(int idLinkedOrganization, String nameLinkedOrganization, String cellPhoneLinkedOrganization, String emailLinkedOrganization) {
        this.idLinkedOrganization = idLinkedOrganization;
        this.nameLinkedOrganization = nameLinkedOrganization;
        this.cellPhoneLinkedOrganization = cellPhoneLinkedOrganization;
        this.emailLinkedOrganization = emailLinkedOrganization;
    }

    public LinkedOrganization() {
        int idLinkedOrganization = 0;
        String nameLinkedOrganization = "";
        String cellPhoneLinkedOrganization = "";
        String emailLinkedOrganization = "";
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
}
