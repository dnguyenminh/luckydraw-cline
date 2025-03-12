package vn.com.fecredit.app.service;

import java.util.List;
import java.util.Map;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;

public interface NotificationService {

    /**
     * Send push notification
     */
    void sendPushNotification(String userId, String title, String message);

    /**
     * Send push notification with data
     */
    void sendPushNotification(String userId, String title, String message, Map<String, String> data);

    /**
     * Send bulk push notifications
     */
    void sendBulkPushNotifications(List<String> userIds, String title, String message);

    /**
     * Send event notification
     */
    void sendEventNotification(Event event, List<Participant> participants, String notificationType);

    /**
     * Send reward won notification
     */
    void sendRewardNotification(Participant participant, Reward reward, Event event);

    /**
     * Send golden hour notification
     */
    void sendGoldenHourNotification(Event event, List<Participant> participants);

    /**
     * Send spin result notification
     */
    void sendSpinResultNotification(Participant participant, Reward reward);

    /**
     * Send system notification
     */
    void sendSystemNotification(String title, String message, List<String> userIds);

    /**
     * Send personal notification
     */
    void sendPersonalNotification(String userId, String title, String message);

    /**
     * Send broadcast notification
     */
    void sendBroadcastNotification(String title, String message);

    /**
     * Register device token
     */
    void registerDeviceToken(String userId, String deviceToken);

    /**
     * Unregister device token
     */
    void unregisterDeviceToken(String userId, String deviceToken);

    /**
     * Get user device tokens
     */
    List<String> getUserDeviceTokens(String userId);

    /**
     * Mark notification as read
     */
    void markNotificationAsRead(String notificationId);

    /**
     * Mark all notifications as read
     */
    void markAllNotificationsAsRead(String userId);

    /**
     * Get user notifications
     */
    List<Map<String, Object>> getUserNotifications(String userId, int limit);

    /**
     * Get unread notifications count
     */
    int getUnreadNotificationsCount(String userId);

    /**
     * Delete notification
     */
    void deleteNotification(String notificationId);

    /**
     * Delete all notifications
     */
    void deleteAllNotifications(String userId);

    /**
     * Check notification settings
     */
    Map<String, Boolean> getNotificationSettings(String userId);

    /**
     * Update notification settings
     */
    void updateNotificationSettings(String userId, Map<String, Boolean> settings);

    /**
     * Enable notifications
     */
    void enableNotifications(String userId);

    /**
     * Disable notifications
     */
    void disableNotifications(String userId);

    /**
     * Schedule notification
     */
    String scheduleNotification(String userId, String title, String message, java.time.LocalDateTime scheduleTime);

    /**
     * Cancel scheduled notification
     */
    boolean cancelScheduledNotification(String scheduleId);

    /**
     * Get notification status
     */
    String getNotificationStatus(String notificationId);

    /**
     * Get notification statistics
     */
    Map<String, Object> getNotificationStatistics(String userId);
}
