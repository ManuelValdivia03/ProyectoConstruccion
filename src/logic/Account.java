package logic;

public class Account {
    private int idUser;
    private String email;
    private String password;

    public Account(int idUser, String email,String password) {
        this.idUser = idUser;
        this.email = email;
        this.password = password;
    }

    public Account(){
        idUser = 0;
        email = "";
        password = "";
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
