package com.example.project_management_app.pipeline.batch;

import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.pipeline.model.TaskMetric;
import com.example.project_management_app.pipeline.model.TeamPerformanceMetric;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @Bean
    public RepositoryItemReader<Task> taskReader() {
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);

        return new RepositoryItemReaderBuilder<Task>()
                .name("taskReader")
                .repository(taskRepository)
                .methodName("findAll")
                .sorts(sorts)
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemProcessor<Task, TaskMetric> taskMetricProcessor() {
        return task -> {
            LocalDate today = LocalDate.now();
            Project project = task.getProject();
            User assignedUser = task.getAssignedUser();

            boolean isCompleted = task.getStatus() == Task.TaskStatus.DONE;
            boolean isOverdue = task.getDueDate() != null &&
                    task.getDueDate().isBefore(LocalDateTime.now()) &&
                    !isCompleted;

            Double completionTimeHours = null;
            if (isCompleted && task.getCreatedAt() != null && task.getUpdatedAt() != null) {
                completionTimeHours = (double) ChronoUnit.HOURS.between(
                        task.getCreatedAt(), task.getUpdatedAt()
                );
            }

            Double taskAgeDays = null;
            if (task.getCreatedAt() != null && !isCompleted) {
                taskAgeDays = (double) ChronoUnit.DAYS.between(
                        task.getCreatedAt(), LocalDateTime.now()
                );
            }

            Long teamId = project.getTeam() != null ? project.getTeam().getId() : null;
            String teamName = project.getTeam() != null ? project.getTeam().getName() : "No Team";

            return TaskMetric.builder()
                    .date(today)
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .teamId(teamId)
                    .teamName(teamName)
                    .totalTasks(1)
                    .completedTasks(isCompleted ? 1 : 0)
                    .inProgressTasks(task.getStatus() == Task.TaskStatus.IN_PROGRESS ? 1 : 0)
                    .overdueTasks(isOverdue ? 1 : 0)
                    .completionRate(isCompleted ? 100.0 : 0.0)
                    .avgCompletionTimeHours(completionTimeHours)
                    .avgTaskAgeDays(taskAgeDays)
                    .activeUsers(assignedUser != null ? 1L : 0L)
                    .uniqueAssignees(assignedUser != null ? 1L : 0L)
                    .calculatedAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public JdbcBatchItemWriter<TaskMetric> taskMetricWriter() {
        return new JdbcBatchItemWriterBuilder<TaskMetric>()
                .dataSource(dataSource)
                .sql("""
                INSERT INTO task_metrics (
                    date, project_id, project_name, team_id, team_name,
                    total_tasks, completed_tasks, in_progress_tasks, overdue_tasks,
                    completion_rate, avg_completion_time_hours, avg_task_age_days,
                    active_users, unique_assignees, calculated_at
                ) VALUES (
                    :date, :projectId, :projectName, :teamId, :teamName,
                    :totalTasks, :completedTasks, :inProgressTasks, :overdueTasks,
                    :completionRate, :avgCompletionTimeHours, :avgTaskAgeDays,
                    :activeUsers, :uniqueAssignees, :calculatedAt
                )
                ON DUPLICATE KEY UPDATE
                    total_tasks = total_tasks + VALUES(total_tasks),
                    completed_tasks = completed_tasks + VALUES(completed_tasks),
                    in_progress_tasks = in_progress_tasks + VALUES(in_progress_tasks),
                    overdue_tasks = overdue_tasks + VALUES(overdue_tasks),
                    calculated_at = VALUES(calculated_at)
                """)
                .beanMapped()
                .build();
    }

    @Bean
    public Step calculateTaskMetricsStep() {
        return new StepBuilder("calculateTaskMetricsStep", jobRepository)
                .<Task, TaskMetric>chunk(1000, transactionManager)
                .reader(taskReader())
                .processor(taskMetricProcessor())
                .writer(taskMetricWriter())
                .build();
    }

    @Bean
    public Job dailyMetricsJob() {
        return new JobBuilder("dailyMetricsJob", jobRepository)
                .start(calculateTaskMetricsStep())
                .build();
    }
}