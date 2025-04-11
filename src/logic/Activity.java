package logic;

import logic.enums.ActivityStatus;

import java.sql.Timestamp;

public class Activity {
    private int idActivity;
    private String nameActivity;
    private String descriptionActivity;
    private ActivityStatus activityStatus;
    private Timestamp startDate;
    private Timestamp endDate;

    public Activity(int idActivity, String nameActivity, String descriptionActivity, ActivityStatus activityStatus, Timestamp startDate, Timestamp endDate) {
        this.idActivity = idActivity;
        this.nameActivity = nameActivity;
        this.descriptionActivity = descriptionActivity;
        this.activityStatus = activityStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Activity(){
        idActivity = 0;
        nameActivity = "";
        descriptionActivity = "";
        activityStatus = ActivityStatus.PENDIENTE;
        startDate = null;
        endDate = null;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public String getNameActivity() {
        return nameActivity;
    }

    public void setNameActivity(String nameActivity) {
        this.nameActivity = nameActivity;
    }

    public String getDescriptionActivity() {
        return descriptionActivity;
    }

    public void setDescriptionActivity(String descriptionActivity) {
        this.descriptionActivity = descriptionActivity;
    }

    public ActivityStatus getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }
}
