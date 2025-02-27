package vn.com.fecredit.app.service;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.User;

import java.util.List;
import java.util.Map;

public interface EmailService {

    /**
     * Send welcome email
     */
    void sendWelcomeEmail(User user);

    /**
     * Send verification email
     */
    void sendVerificationEmail(User user, String verificationToken);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(User user, String resetToken);

    /**
     * Send password changed notification
     */
    void sendPasswordChangedEmail(User user);

    /**
     * Send reward won notification
     */
    void sendRewardWonEmail(Participant participant, Reward reward, Event event);

    /**
     * Send event invitation
     */
    void sendEventInvitation(Event event, List<Participant> participants);

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
    void sendSpinResultEmail(Participant participant, Reward reward, Event event);

    /**
     * Send daily summary
     */
    void sendDailySummary(Event event, Map<String, Object> statistics);

    /**
     * Send weekly report
     */
    void sendWeeklyReport(Event event, Map<String, Object> statistics);

    /**
     * Send monthly report
     */
    void sendMonthlyReport(Event event, Map<String, Object> statistics);

    /**
     * Send account blocked notification
     */
    void sendAccountBlockedEmail(User user, String reason);

    /**
     * Send account unblocked notification
     */
    void sendAccountUnblockedEmail(User user);

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
    void sendSpinLimitReachedEmail(Participant participant, Event event);

    /**
     * Send custom email
     */
    void sendCustomEmail(String to, String subject, String content);

    /**
     * Send bulk custom email
     */
    void sendBulkCustomEmail(List<String> recipients, String subject, String content);

    /**
     * Send email with template
     */
    void sendTemplateEmail(String to, String templateName, Map<String, Object> templateData);

    /**
     * Send email with attachment
     */
    void sendEmailWithAttachment(String to, String subject, String content, String attachmentPath);

    /**
     * Get email template
     */
    String getEmailTemplate(String templateName);

    /**
     * Process email template
     */
    String processTemplate(String template, Map<String, Object> data);
}
