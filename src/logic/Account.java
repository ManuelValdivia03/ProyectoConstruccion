package logic;

public class Account {
    private int idUser;
    private String password;
    private String email;

    public Account(int idUser, String password, String email) {
        this.idUser = idUser;
        this.password = password;
        this.email = email;
    }

    public Account(){
        idUser = 0;
        password = "";
        email = "";
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
