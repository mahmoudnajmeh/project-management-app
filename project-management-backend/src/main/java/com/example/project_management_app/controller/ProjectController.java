package com.example.project_management_app.controller;

import com.example.project_management_app.dto.ProjectDto;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);

            // Check if user has access to this project
            User currentUser = accountService.getCurrentUser();
            if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).body("Access denied");
            }

            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body("Project not found");
            }
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error");
        }
    }

    @GetMapping("/my-projects")
    public ResponseEntity<?> getMyProjects() {
        try {
            List<Project> projects = projectService.getProjectsByUser();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error");
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDto projectDto) {
        try {
            Project project = projectService.createProject(projectDto);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDto projectDto) {
        try {
            Project project = projectService.updateProject(id, projectDto);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProjects(@RequestParam String name) {
        try {
            List<Project> projects = projectService.searchProjects(name);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error");
        }
    }
}