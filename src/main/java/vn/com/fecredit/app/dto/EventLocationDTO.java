package vn.com.fecredit.app.dto;

import lombok.*;
import vn.com.fecredit.app.enums.EntityStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLocationDTO {
    private Long id;
    private String name;
    private String province;
    private String district;
    private String ward;
    private String address;
    private String city;
    private Long eventId;
    private String eventName;
    private EntityStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime lastModifiedAt;
    private String lastModifiedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String name;
        private String province;
        private String district;
        private String ward;
        private String address;
        private String city;
        private Long eventId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Long id;
        private String name;
        private String province;
        private String district;
        private String ward;
        private String address;
        private String city;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventLocationResponse {
        private Long id;
        private String name;
        private String province;
        private String district;
        private String ward; 
        private String address;
        private String city;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventLocationSummary {
        private Long id;
        private String name;
        private String province;
        private String city;
        private Boolean isActive;
    }
}
