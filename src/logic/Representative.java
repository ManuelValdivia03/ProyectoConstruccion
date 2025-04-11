package logic;

public class Representative {
    private int idRepresentative;
    private String fullName;
    private String email;
    private String cellPhone;
    private LinkedOrganization linkedOrganization; // Relación con organización vinculada

    public Representative(int idRepresentative, String fullName, String email,
                          String cellPhone, LinkedOrganization linkedOrganization) {
        this.idRepresentative = idRepresentative;
        this.fullName = fullName;
        this.email = email;
        this.cellPhone = cellPhone;
        this.linkedOrganization = linkedOrganization;
    }

    public Representative() {
        idRepresentative = 0;
        fullName = "";
        email = "";
        cellPhone = "";
        linkedOrganization = null;
    }

    public int getIdRepresentative() {
        return idRepresentative;
    }

    public void setIdRepresentative(int idRepresentative) {
        this.idRepresentative = idRepresentative;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public LinkedOrganization getLinkedOrganization() {
        return linkedOrganization;
    }

    public void setLinkedOrganization(LinkedOrganization linkedOrganization) {
        this.linkedOrganization = linkedOrganization;
    }
}
