package logic.enums;

public enum ActivityStatus {
    Pendiente("Pendiente"),
    Completada("Completada"),
    En_progreso("En progreso"),
    Cancelada("Cancelada");

    private final String dbValue;

    ActivityStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ActivityStatus fromDbValue(String dbValue) {
        for (ActivityStatus status : values()) {
            if (status.getDbValue().equalsIgnoreCase(dbValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for db value: " + dbValue);
    }
}
