package vn.com.fecredit.app.util;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.enums.LocationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventLocationTestBuilder {
    
    public static EventLocation createTestLocation() {
        return createTestLocation(EntityStatus.ACTIVE);
    }

    public static EventLocation createTestLocation(EntityStatus status) {
        EventLocation location = new EventLocation();
        location.setName("Test Location");
        location.setCode("LOC-" + UUID.randomUUID().toString().substring(0, 8));
        location.setAddress("123 Test Street");
        location.setDistrict("Test District");
        location.setCity("Test City");
        location.setProvince("Test Province");
        location.setPostalCode("12345");
        location.setLatitude(10.762622);
        location.setLongitude(106.660172);
        location.setType(LocationType.BRANCH);
        location.setDailySpinLimit(100);
        location.setTotalSpins(0);
        location.setStatus(status);
        location.setWinProbabilityMultiplier(1.0);
        location.setSortOrder(1);
        return location;
    }

    public static EventLocation createTestLocationWithEvent(Event event) {
        EventLocation location = createTestLocation();
        location.setEvent(event);
        return location;
    }

    public static EventLocation createTestLocationWithCoordinates(double latitude, double longitude) {
        EventLocation location = createTestLocation();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public static EventLocation createTestLocationWithAddress(String address, String district, 
                                                            String city, String province) {
        EventLocation location = createTestLocation();
        location.setAddress(address);
        location.setDistrict(district);
        location.setCity(city);
        location.setProvince(province);
        return location;
    }

    public static EventLocation createTestLocationWithType(LocationType type) {
        EventLocation location = createTestLocation();
        location.setType(type);
        return location;
    }

    public static EventLocation createTestLocationWithSpinLimits(int dailyLimit) {
        EventLocation location = createTestLocation();
        location.setDailySpinLimit(dailyLimit);
        location.setTotalSpins(0);
        return location;
    }

    public static EventLocation createTestLocationWithProbability(double multiplier) {
        EventLocation location = createTestLocation();
        location.setWinProbabilityMultiplier(multiplier);
        return location;
    }

    public static EventLocation createMaxProbabilityLocation() {
        EventLocation location = createTestLocation();
        location.setWinProbabilityMultiplier(2.0);
        return location;
    }

    public static EventLocation createMinProbabilityLocation() {
        EventLocation location = createTestLocation();
        location.setWinProbabilityMultiplier(0.5);
        return location;
    }

    public static EventLocation createUnlimitedSpinsLocation() {
        EventLocation location = createTestLocation();
        location.setDailySpinLimit(null);
        return location;
    }

    public static EventLocation createFullyUsedLocation() {
        EventLocation location = createTestLocation();
        location.setDailySpinLimit(100);
        location.setTotalSpins(100);
        location.setLastSpin(LocalDateTime.now());
        return location;
    }

    public static EventLocation createPriorityLocation(int sortOrder) {
        EventLocation location = createTestLocation();
        location.setSortOrder(sortOrder);
        return location;
    }

    public static EventLocation createTestLocationWithMetadata(String metadata) {
        EventLocation location = createTestLocation();
        location.setMetadata(metadata);
        return location;
    }
}
