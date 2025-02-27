package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SchedulerService {

    /**
     * Schedule a one-time task
     */
    String scheduleOneTimeTask(String taskName, Runnable task, LocalDateTime executionTime);

    /**
     * Schedule a recurring task
     */
    String scheduleRecurringTask(String taskName, Runnable task, String cronExpression);

    /**
     * Schedule a task with custom configuration
     */
    String scheduleTask(TaskConfig taskConfig);

    /**
     * Cancel scheduled task
     */
    boolean cancelTask(String taskId);

    /**
     * Pause task
     */
    void pauseTask(String taskId);

    /**
     * Resume task
     */
    void resumeTask(String taskId);

    /**
     * Get task details
     */
    TaskInfo getTaskInfo(String taskId);

    /**
     * Get all scheduled tasks
     */
    List<TaskInfo> getAllTasks();

    /**
     * Get active tasks
     */
    List<TaskInfo> getActiveTasks();

    /**
     * Get completed tasks
     */
    List<TaskInfo> getCompletedTasks();

    /**
     * Get failed tasks
     */
    List<TaskInfo> getFailedTasks();

    /**
     * Get task execution history
     */
    List<TaskExecution> getTaskExecutionHistory(String taskId);

    /**
     * Clear task history
     */
    void clearTaskHistory(String taskId);

    /**
     * Get scheduler status
     */
    SchedulerStatus getSchedulerStatus();

    /**
     * Start scheduler
     */
    void startScheduler();

    /**
     * Stop scheduler
     */
    void stopScheduler();

    /**
     * Pause all tasks
     */
    void pauseAllTasks();

    /**
     * Resume all tasks
     */
    void resumeAllTasks();

    /**
     * Task configuration class
     */
    @lombok.Data
    @lombok.Builder
    class TaskConfig {
        private String taskName;
        private Runnable task;
        private String cronExpression;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long initialDelay;
        private Long fixedDelay;
        private Long fixedRate;
        private Integer retryCount;
        private Long retryDelay;
        private Map<String, Object> data;
        private String description;
        private String category;
        private Integer priority;
        private Boolean concurrent;
    }

    /**
     * Task information class
     */
    @lombok.Data
    class TaskInfo {
        private final String taskId;
        private final String taskName;
        private final String status;
        private final LocalDateTime nextExecutionTime;
        private final LocalDateTime lastExecutionTime;
        private final String cronExpression;
        private final Long executionCount;
        private final Long failureCount;
        private final String lastError;
        private final Map<String, Object> data;
    }

    /**
     * Task execution class
     */
    @lombok.Data
    class TaskExecution {
        private final String taskId;
        private final String taskName;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String status;
        private final String error;
        private final Long duration;
    }

    /**
     * Scheduler status class
     */
    @lombok.Data
    class SchedulerStatus {
        private final boolean running;
        private final int activeCount;
        private final int queueSize;
        private final int completedTaskCount;
        private final int failedTaskCount;
        private final LocalDateTime startTime;
        private final String state;
    }
}
