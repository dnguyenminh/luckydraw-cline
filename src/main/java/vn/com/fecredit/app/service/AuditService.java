package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {

    /**
     * Log audit event
     */
    void logEvent(AuditEvent event);

    /**
     * Log system event
     */
    void logSystemEvent(String action, String description);

    /**
     * Log user action
     */
    void logUserAction(String username, String action, String description);

    /**
     * Log data change
     */
    void logDataChange(String username, String entityType, Long entityId, 
                      Map<String, Object> oldValues, Map<String, Object> newValues);

    /**
     * Log security event
     */
    void logSecurityEvent(String username, String action, String description, String ipAddress);

    /**
     * Get audit events by criteria
     */
    Page<AuditEvent> getAuditEvents(SearchRequest searchRequest);

    /**
     * Get audit events by date range
     */
    List<AuditEvent> getAuditEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get audit events by user
     */
    List<AuditEvent> getAuditEventsByUser(String username);

    /**
     * Get audit events by entity
     */
    List<AuditEvent> getAuditEventsByEntity(String entityType, Long entityId);

    /**
     * Get audit events by action
     */
    List<AuditEvent> getAuditEventsByAction(String action);

    /**
     * Get audit event details
     */
    AuditEvent getAuditEventDetails(Long eventId);

    /**
     * Get latest audit events
     */
    List<AuditEvent> getLatestAuditEvents(int limit);

    /**
     * Get audit summary
     */
    AuditSummary getAuditSummary(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Export audit events
     */
    byte[] exportAuditEvents(SearchRequest searchRequest, String format);

    /**
     * Clean up old audit events
     */
    void cleanupAuditEvents(LocalDateTime beforeDate);

    /**
     * Audit event class
     */
    @lombok.Data
    @lombok.Builder
    class AuditEvent {
        private Long id;
        private LocalDateTime timestamp;
        private String username;
        private String action;
        private String description;
        private String entityType;
        private Long entityId;
        private Map<String, Object> oldValues;
        private Map<String, Object> newValues;
        private String ipAddress;
        private String userAgent;
        private String sessionId;
        private String status;
        private String errorMessage;
        private Map<String, Object> metadata;
    }

    /**
     * Audit summary class
     */
    @lombok.Data
    class AuditSummary {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final long totalEvents;
        private final Map<String, Long> eventsByAction;
        private final Map<String, Long> eventsByUser;
        private final Map<String, Long> eventsByEntityType;
        private final Map<String, Long> eventsByStatus;
        private final long successCount;
        private final long failureCount;
        private final Map<String, Object> statistics;
    }
}
