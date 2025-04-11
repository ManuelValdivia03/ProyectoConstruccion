package logic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ActivityCronogram {
    private int idCronogram;
    private Timestamp dateStart;
    private Timestamp dateEnd;
    private List<Activity> activities;

    public ActivityCronogram(int idCronogram, Timestamp dateStart, Timestamp dateEnd, List<Activity> activities) {
        this.idCronogram = idCronogram;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.activities = activities;
    }

    public ActivityCronogram() {
        idCronogram = 0;
        dateStart = null;
        dateEnd = null;
        activities = new ArrayList<Activity>();
    }

    public int getIdCronogram() {
        return idCronogram;
    }

    public void setIdCronogram(int idCronogram) {
        this.idCronogram = idCronogram;
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

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
