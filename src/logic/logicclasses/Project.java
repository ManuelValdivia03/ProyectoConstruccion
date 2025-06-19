package logic.logicclasses;

import java.sql.Timestamp;

public class Project {
    private int idProyect;
    private String title;
    private String description;
    private Timestamp dateStart;
    private Timestamp dateEnd;
    private char status;
    private int capacity;
    private int currentStudents;

    public Project(int idProyect, String title, String description,
                   Timestamp dateStart, Timestamp dateEnd,
                   char status, int capacity, int currentStudents) {
        this.idProyect = idProyect;
        this.title = title;
        this.description = description;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.status = status;
        this.capacity = capacity;
        this.currentStudents = currentStudents;
    }

    public Project() {
        idProyect = 0;
        title = "";
        description = "";
        dateStart = Timestamp.valueOf("2000-01-01 00:00:00");
        dateEnd = Timestamp.valueOf("2000-01-01 00:00:00");
        status = 'A';
        capacity = 0;
        currentStudents = 0;
    }

    public int getIdProyect() {
        return idProyect;
    }

    public void setIdProyect(int idProyect) {
        this.idProyect = idProyect;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDateStart() {
        return dateStart;
    }

    public void setDateStart(Timestamp dateStart) {
        this.dateStart = dateStart;
    }

    public Timestamp getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Timestamp dateEnd) {
        this.dateEnd = dateEnd;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentStudents() {
        return currentStudents;
    }

    public void setCurrentStudents(int currentStudents) {
        this.currentStudents = currentStudents;
    }



}
