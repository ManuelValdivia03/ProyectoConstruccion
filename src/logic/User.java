package logic;

public class User {
    private int idUser;
    private String fullName;
    private String cellphone;

    public User(int idUser, String fullName, String cellphone) {
        this.idUser = idUser;
        this.fullName = fullName;
        this.cellphone = cellphone;
    }

    public User() {
        idUser = 0;
        fullName = "";
        cellphone = "";
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

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }
}
