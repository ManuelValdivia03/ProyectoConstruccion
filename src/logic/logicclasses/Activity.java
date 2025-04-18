package logic.logicclasses;

import logic.enums.ActivityStatus;

import java.sql.Timestamp;

public class Activity {
    private int idActivity;
    private String nameActivity;
    private String descriptionActivity;
    private Timestamp startDate;
    private Timestamp endDate;
    private ActivityStatus activityStatus;

    public Activity(int idActivity, String nameActivity, String descriptionActivity, Timestamp startDate, Timestamp endDate, ActivityStatus activityStatus) {
        this.idActivity = idActivity;
        this.nameActivity = nameActivity;
        this.descriptionActivity = descriptionActivity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activityStatus = activityStatus;
    }

    public Activity(){
        idActivity = 0;
        nameActivity = "";
        descriptionActivity = "";
        startDate = null;
        endDate = null;
        activityStatus = ActivityStatus.Pendiente;
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
