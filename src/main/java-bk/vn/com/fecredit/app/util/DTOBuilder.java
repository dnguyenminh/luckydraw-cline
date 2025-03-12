package vn.com.fecredit.app.util;

import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.enums.EventStatus;
import vn.com.fecredit.app.enums.LocationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Utility class to help build DTOs while Lombok is being configured
 */
public class DTOBuilder {
    
    public static EventLocationDTO buildEventLocation() {
        EventLocationDTO dto = new EventLocationDTO();
        
        // Basic fields
        dto.setId(null);
        dto.setEventId(null);
        dto.setName("");
        dto.setDescription("");
        dto.setAddress("");
        dto.setEventName("");
        
        // Address components
        dto.setAddressLine1("");
        dto.setAddressLine2("");
        dto.setDistrict("");
        dto.setCity("");
        dto.setProvince("");
        dto.setPostalCode("");
        
        // Status and metadata
        dto.setStatus(EventStatus.DRAFT);
        dto.setType(LocationType.STORE);
        dto.setMetadata(new HashMap<>());
        dto.setTags(new ArrayList<>());
        
        // Audit fields
        LocalDateTime now = LocalDateTime.now();
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        dto.setCreatedBy("");
        dto.setUpdatedBy("");
        
        // Game mechanics
        dto.setDailySpinLimit(100);
        dto.setTotalSpins(0);
        dto.setRemainingSpins(0);
        dto.setWinningSpins(0);
        dto.setWinProbabilityMultiplier(1.0);
        dto.setActive(true);
        dto.setDeleted(false);
        
        // Location coordinates
        dto.setLatitude(0.0);
        dto.setLongitude(0.0);
        
        return dto;
    }
}
