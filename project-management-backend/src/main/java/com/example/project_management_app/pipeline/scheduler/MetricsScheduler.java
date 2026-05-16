package com.example.project_management_app.pipeline.scheduler;

import com.example.project_management_app.pipeline.service.RealTimeMetricsService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class MetricsScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job dailyMetricsJob;

    @Autowired
    private RealTimeMetricsService realTimeMetricsService;

    // Run daily at 1 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyMetricsJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("timestamp", LocalDateTime.now().toString())
                .toJobParameters();

        jobLauncher.run(dailyMetricsJob, params);
    }

    // Reset daily counters at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyCounters() {
        realTimeMetricsService.resetDailyMetrics();
    }
}