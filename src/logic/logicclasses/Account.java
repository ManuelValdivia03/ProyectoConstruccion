package logic.logicclasses;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return idUser == account.idUser &&
                java.util.Objects.equals(email, account.email) &&
                java.util.Objects.equals(password, account.password);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idUser, email, password);
    }
}
