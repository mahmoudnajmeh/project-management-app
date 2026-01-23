package com.example.project_management_app.service;

import com.example.project_management_app.dto.TaskDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private AccountService accountService;

    @Override
    public Task createTask(TaskDto taskDto) {
        Project project = projectRepository.findById(taskDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User assignedUser = userRepository.findById(taskDto.getAssignedUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Long id, TaskDto taskDto) {
        Task task = getTaskById(id);

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
        if (taskDto.getAssignedUserId() != null &&
                !taskDto.getAssignedUserId().equals(task.getAssignedUser().getId())) {
            User newAssignedUser = userRepository.findById(taskDto.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedUser(newAssignedUser);
            newAssignedUser.setLastActivity(LocalDateTime.now());
            userRepository.save(newAssignedUser);
        }

        task.getAssignedUser().setLastActivity(LocalDateTime.now());
        userRepository.save(task.getAssignedUser());

        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        task.getAssignedUser().setLastActivity(LocalDateTime.now());
        userRepository.save(task.getAssignedUser());
        taskRepository.deleteById(id);
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
        return taskRepository.findByAssignedUser(currentUser);
    }
}