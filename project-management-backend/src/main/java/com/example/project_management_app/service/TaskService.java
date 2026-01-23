package com.example.project_management_app.service;


import com.example.project_management_app.dto.TaskDto;
import com.example.project_management_app.entity.Task;

import java.util.List;

public interface TaskService {
    Task createTask(TaskDto taskDto);
    Task updateTask(Long id, TaskDto taskDto);
    void deleteTask(Long id);
    Task getTaskById(Long id);
    List<Task> getAllTasks();
    List<Task> getTasksByProject(Long projectId);
    List<Task> getTasksByUser();
}