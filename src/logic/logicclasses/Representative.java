package logic.logicclasses;

public class Representative {
    private int idRepresentative;
    private String fullName;
    private String email;
    private String cellPhone;
    private LinkedOrganization linkedOrganization;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Representative that = (Representative) o;
        return idRepresentative == that.idRepresentative &&
                java.util.Objects.equals(fullName, that.fullName) &&
                java.util.Objects.equals(email, that.email) &&
                java.util.Objects.equals(cellPhone, that.cellPhone) &&
                java.util.Objects.equals(linkedOrganization, that.linkedOrganization);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idRepresentative, fullName, email, cellPhone, linkedOrganization);
    }
}
