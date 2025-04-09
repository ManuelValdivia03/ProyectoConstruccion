package logic;

import java.sql.Timestamp;

public class Proyect {
    private int idProyect;
    private String title;
    private String description;
    private Timestamp dateStart;
    private Timestamp dateEnd;

    public Proyect(int idProyect, String title, String description, Timestamp dateStart, Timestamp dateEnd) {
        this.idProyect = idProyect;
        this.title = title;
        this.description = description;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public Proyect(){
        idProyect = 0;
        title = "";
        description = "";
        dateStart = null;
        dateEnd = null;
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
}
