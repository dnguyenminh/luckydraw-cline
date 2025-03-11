package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.User;

import java.util.List;
import java.util.Map;

public interface SMSService {

    /**
     * Send verification code
     */
    void sendVerificationCode(String phoneNumber, String code);

    /**
     * Send welcome message
     */
    void sendWelcomeMessage(User user);

    /**
     * Send password reset code
     */
    void sendPasswordResetCode(User user, String resetCode);

    /**
     * Send reward won notification
     */
    void sendRewardWonMessage(Participant participant, Reward reward, Event event);

    /**
     * Send event invitation
     */
    void sendEventInvitation(Event event, Participant participant);

    /**
     * Send event reminder
     */
    void sendEventReminder(Event event, Participant participant);

    /**
     * Send event start notification
     */
    void sendEventStartNotification(Event event, List<Participant> participants);

    /**
     * Send event end notification
     */
    void sendEventEndNotification(Event event, List<Participant> participants);

    /**
     * Send golden hour notification
     */
    void sendGoldenHourNotification(Event event, List<Participant> participants);

    /**
     * Send spin result notification
     */
    void sendSpinResultMessage(Participant participant, Reward reward, Event event);

    /**
     * Send account blocked notification
     */
    void sendAccountBlockedMessage(User user);

    /**
     * Send account unblocked notification
     */
    void sendAccountUnblockedMessage(User user);

    /**
     * Send reward claim instructions
     */
    void sendRewardClaimInstructions(Participant participant, Reward reward);

    /**
     * Send reward expiry reminder
     */
    void sendRewardExpiryReminder(Participant participant, Reward reward);

    /**
     * Send spin limit reached notification
     */
    void sendSpinLimitReachedMessage(Participant participant, Event event);

    /**
     * Send custom SMS
     */
    void sendCustomMessage(String phoneNumber, String message);

    /**
     * Send bulk custom SMS
     */
    void sendBulkCustomMessage(List<String> phoneNumbers, String message);

    /**
     * Send SMS with template
     */
    void sendTemplateMessage(String phoneNumber, String templateName, Map<String, Object> templateData);

    /**
     * Get SMS template
     */
    String getSMSTemplate(String templateName);

    /**
     * Process SMS template
     */
    String processTemplate(String template, Map<String, Object> data);

    /**
     * Validate phone number
     */
    boolean isValidPhoneNumber(String phoneNumber);

    /**
     * Format phone number
     */
    String formatPhoneNumber(String phoneNumber);

    /**
     * Get delivery status
     */
    String getMessageStatus(String messageId);

    /**
     * Check if SMS service is available
     */
    boolean isServiceAvailable();

    /**
     * Get remaining SMS credits
     */
    int getRemainingCredits();

    /**
     * Get SMS delivery report
     */
    Map<String, Object> getDeliveryReport(String messageId);

    /**
     * Schedule SMS
     */
    String scheduleSMS(String phoneNumber, String message, java.time.LocalDateTime scheduleTime);

    /**
     * Cancel scheduled SMS
     */
    boolean cancelScheduledSMS(String scheduleId);
}
