package logic;

public class Coordinator extends User{

    String staffNumber;

    public Coordinator(int idUser, String fullName, String cellPhone, String staffNumber) {
        super(idUser, fullName, cellPhone);
        this.staffNumber = staffNumber;
    }

    public Coordinator(){
        super();
        staffNumber = "";
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        this.staffNumber = staffNumber;
    }

}
