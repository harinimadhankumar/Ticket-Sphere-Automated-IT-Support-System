package com.powergrid.ticketsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * ============================================================
 * SCHEDULER CONFIGURATION
 * ============================================================
 * 
 * Configuration for Spring Scheduler used by Email Ingestion Service.
 * 
 * PURPOSE:
 * - Enables scheduled task execution
 * - Configures thread pool for scheduled tasks
 * - Used by EmailIngestionService for periodic inbox polling
 * 
 * SCHEDULE:
 * - Email polling runs every 5 minutes (configurable)
 * - Uses a dedicated thread pool to avoid blocking main threads
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    /**
     * Creates and configures the TaskScheduler bean.
     * 
     * Thread pool configuration:
     * - Pool size: 2 threads (sufficient for email polling)
     * - Thread name prefix: "email-scheduler-"
     * - Error handler: Logs errors without stopping scheduler
     * 
     * @return Configured ThreadPoolTaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {

        logger.info("Initializing Task Scheduler for email ingestion");

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Configure thread pool
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("email-scheduler-");

        // Configure error handling - continue on errors
        scheduler.setErrorHandler(throwable -> {
            logger.error("Scheduled task error: {}", throwable.getMessage(), throwable);
        });

        // Wait for tasks to complete on shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        scheduler.initialize();

        logger.info("Task Scheduler initialized with pool size: 2");

        return scheduler;
    }
}
