package logic.enums;

public enum RequestStatus {
    PENDIENTE("pendiente"),
    APROBADA("aceptada"),
    RECHAZADA("rechazada");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RequestStatus fromDisplayName(String displayName) {
        for (RequestStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown displayName: " + displayName);
    }
}
