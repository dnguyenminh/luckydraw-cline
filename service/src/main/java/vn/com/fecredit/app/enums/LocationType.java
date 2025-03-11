package vn.com.fecredit.app.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines the different types of locations where lucky draw events can be accessed
 */
public enum LocationType {
    
    BRANCH("BRANCH", "FE Credit Branch Office"),
    KIOSK("KIOSK", "Self-Service Kiosk"),
    MOBILE("MOBILE", "Mobile App"),
    WEB("WEB", "Website"),
    POS("POS", "Point of Sale Terminal"),
    PARTNER("PARTNER", "Partner Location");

    private final String code;
    private final String description;

    LocationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LocationType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (LocationType type : LocationType.values()) {
            if (type.code.equals(code.toUpperCase())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown location type code: " + code);
    }

    public boolean isPhysicalLocation() {
        return this == BRANCH || this == KIOSK || this == POS || this == PARTNER;
    }

    public boolean isDigitalLocation() {
        return this == MOBILE || this == WEB;
    }

    public boolean requiresStaffPresence() {
        return this == BRANCH || this == POS || this == PARTNER;
    }
}
