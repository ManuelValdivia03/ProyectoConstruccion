package logic.logicclasses;

public class Coordinator extends User {

    String staffNumber;

    public Coordinator(int idUser, String fullName, String cellPhone, String phoneExtension,String staffNumber, char status) {
        super(idUser, fullName, cellPhone, phoneExtension, status);
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
