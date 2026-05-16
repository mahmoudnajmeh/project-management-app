package com.example.project_management_app.service;

import com.example.project_management_app.dto.TaskDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.pipeline.event.ActivityEventPublisher;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivityEventPublisher activityEventPublisher;

    @Override
    public Task createTask(TaskDto taskDto) {
        Project project = projectRepository.findById(taskDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User assignedUser = userRepository.findById(taskDto.getAssignedUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User currentUser = accountService.getCurrentUser();

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setProject(project);
        task.setAssignedUser(assignedUser);
        task.setStatus(taskDto.getStatus() != null ? taskDto.getStatus() : Task.TaskStatus.TODO);
        task.setPriority(taskDto.getPriority() != null ? taskDto.getPriority() : Task.Priority.MEDIUM);

        if (taskDto.getDueDate() != null && !taskDto.getDueDate().isEmpty()) {
            task.setDueDate(LocalDateTime.parse(taskDto.getDueDate()));
        }

        assignedUser.setLastActivity(LocalDateTime.now());
        userRepository.save(assignedUser);

        Task savedTask = taskRepository.save(task);

        List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

        String notificationContent = currentUser.getFirstName() + " created task: " + task.getTitle() + " in project: " + project.getName();
        notificationService.createNotificationForUsers(
                teamMembers,
                notificationContent,
                "TASK_CREATED",
                savedTask.getId(),
                "TASK",
                currentUser.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        // Publish event for data pipeline
        activityEventPublisher.publishTaskCreated(savedTask, currentUser);

        return savedTask;
    }

    @Override
    public Task updateTask(Long id, TaskDto taskDto) {
        Task task = getTaskById(id);
        User currentUser = accountService.getCurrentUser();

        // Check authorization FIRST
        boolean canUpdate = checkTaskAuthorization(task, currentUser);
        if (!canUpdate) {
            throw new RuntimeException("You are not authorized to update this task");
        }

        boolean isStatusChanged = taskDto.getStatus() != null && !task.getStatus().equals(taskDto.getStatus());
        boolean isAssignedUserChanged = taskDto.getAssignedUserId() != null &&
                (task.getAssignedUser() == null || !taskDto.getAssignedUserId().equals(task.getAssignedUser().getId()));

        if (taskDto.getTitle() != null) {
            task.setTitle(taskDto.getTitle());
        }
        if (taskDto.getDescription() != null) {
            task.setDescription(taskDto.getDescription());
        }
        if (taskDto.getStatus() != null) {
            task.setStatus(taskDto.getStatus());
        }
        if (taskDto.getPriority() != null) {
            task.setPriority(taskDto.getPriority());
        }
        if (taskDto.getDueDate() != null && !taskDto.getDueDate().isEmpty()) {
            task.setDueDate(LocalDateTime.parse(taskDto.getDueDate()));
        }
        if (isAssignedUserChanged && taskDto.getAssignedUserId() != null) {
            User newAssignedUser = userRepository.findById(taskDto.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedUser(newAssignedUser);
            newAssignedUser.setLastActivity(LocalDateTime.now());
            userRepository.save(newAssignedUser);
        }

        if (task.getAssignedUser() != null) {
            task.getAssignedUser().setLastActivity(LocalDateTime.now());
            userRepository.save(task.getAssignedUser());
        }

        Task updatedTask = taskRepository.save(task);

        List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

        String notificationType;
        String action;

        if (isStatusChanged && task.getStatus() == Task.TaskStatus.DONE) {
            notificationType = "TASK_COMPLETED";
            action = "completed task: " + task.getTitle();
        } else if (isAssignedUserChanged) {
            notificationType = "TASK_REASSIGNED";
            action = "reassigned task: " + task.getTitle();
        } else {
            notificationType = "TASK_UPDATED";
            action = "updated task: " + task.getTitle();
        }

        String notificationContent = currentUser.getFirstName() + " " + action + " in project: " + task.getProject().getName();
        notificationService.createNotificationForUsers(
                teamMembers,
                notificationContent,
                notificationType,
                task.getId(),
                "TASK",
                currentUser.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        // Publish event ONLY when task is completed
        if (isStatusChanged && task.getStatus() == Task.TaskStatus.DONE) {
            activityEventPublisher.publishTaskCompleted(updatedTask, currentUser);
        }

        return updatedTask;
    }

    @Override
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        User currentUser = accountService.getCurrentUser();

        boolean canDelete = checkTaskAuthorization(task, currentUser);
        if (!canDelete) {
            throw new RuntimeException("You are not authorized to delete this task");
        }

        if (task.getAssignedUser() != null) {
            task.getAssignedUser().setLastActivity(LocalDateTime.now());
            userRepository.save(task.getAssignedUser());
        }

        List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

        String notificationContent = currentUser.getFirstName() + " deleted task: " + task.getTitle() + " from project: " + task.getProject().getName();
        notificationService.createNotificationForUsers(
                teamMembers,
                notificationContent,
                "TASK_DELETED",
                task.getId(),
                "TASK",
                currentUser.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        taskRepository.deleteById(id);
    }

    private boolean checkTaskAuthorization(Task task, User currentUser) {
        if (task.getAssignedUser() != null && task.getAssignedUser().getId().equals(currentUser.getId())) {
            return true;
        }

        if (task.getProject().getCreatedBy() != null &&
                task.getProject().getCreatedBy().getId().equals(currentUser.getId())) {
            return true;
        }

        if (task.getProject().getTeam() != null &&
                task.getProject().getTeam().getCreatedBy() != null &&
                task.getProject().getTeam().getCreatedBy().getId().equals(currentUser.getId())) {
            return true;
        }

        return false;
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return taskRepository.findByProject(project);
    }

    @Override
    public List<Task> getTasksByUser() {
        User currentUser = accountService.getCurrentUser();
        currentUser.setLastActivity(LocalDateTime.now());
        userRepository.save(currentUser);

        Set<Task> allTasks = new HashSet<>();

        List<Task> assignedTasks = taskRepository.findByAssignedUser(currentUser);
        allTasks.addAll(assignedTasks);

        List<Project> createdProjects = projectRepository.findByCreatedBy(currentUser);
        for (Project project : createdProjects) {
            List<Task> projectTasks = taskRepository.findByProject(project);
            allTasks.addAll(projectTasks);
        }

        List<Team> userTeams = teamRepository.findByMemberId(currentUser.getId());
        for (Team team : userTeams) {
            List<Project> teamProjects = projectRepository.findByTeam(team);
            for (Project project : teamProjects) {
                List<Task> projectTasks = taskRepository.findByProject(project);
                allTasks.addAll(projectTasks);
            }
        }

        List<Project> projectsWithAssignedTasks = projectRepository.findByTasksAssignedUser(currentUser);
        for (Project project : projectsWithAssignedTasks) {
            List<Task> projectTasks = taskRepository.findByProject(project);
            allTasks.addAll(projectTasks);
        }

        return new ArrayList<>(allTasks);
    }

    @Override
    public List<Task> getTasksByAssignedUser(User user) {
        return taskRepository.findByAssignedUser(user);
    }
}