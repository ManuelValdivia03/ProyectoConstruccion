package logic.logicclasses;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private int nrc;
    private String groupName;
    private List<Student> students;

    public Group(int nrc, String groupName, List<Student> students) {
        this.nrc = nrc;
        this.groupName = groupName;
        this.students = students;
    }

    public Group() {
        nrc = 0;
        groupName = "";
        students = new ArrayList<>();
    }

    public int getNrc() {
        return nrc;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setNrc(int nrc) {
        this.nrc = nrc;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
