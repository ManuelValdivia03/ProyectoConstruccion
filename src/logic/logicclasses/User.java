package logic.logicclasses;

public class User {
    private int idUser;
    private String fullName;
    private String cellphone;
    private char status;

    public User(int idUser, String fullName, String cellphone, char status) {
        this.idUser = idUser;
        this.fullName = fullName;
        this.cellphone = cellphone;
        this.status = status;
    }

    public User() {
        idUser = 0;
        fullName = "";
        cellphone = "";
        status = 'A';
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCellPhone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
}
