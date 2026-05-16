package com.example.project_management_app.controller;

import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String q) {
        User currentUser = accountService.getCurrentUser();
        List<Map<String, Object>> results = new ArrayList<>();

        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(results);
        }

        String searchTerm = "%" + q.toLowerCase() + "%";

        List<Project> projects = projectRepository.findByNameContainingIgnoreCase(q);
        for (Project p : projects) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", p.getId());
            result.put("title", p.getName());
            result.put("description", p.getDescription());
            result.put("type", "project");
            result.put("url", "/projects/" + p.getId());
            results.add(result);
        }

        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCase(q);
        for (Task t : tasks) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", t.getId());
            result.put("title", t.getTitle());
            result.put("description", t.getDescription());
            result.put("type", "task");
            result.put("projectName", t.getProject() != null ? t.getProject().getName() : null);
            result.put("url", "/tasks/" + t.getId());
            results.add(result);
        }

        List<Team> teams = teamRepository.findByNameContainingIgnoreCase(q);
        for (Team t : teams) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", t.getId());
            result.put("title", t.getName());
            result.put("description", t.getDescription());
            result.put("type", "team");
            result.put("url", "/team/" + t.getId());
            results.add(result);
        }

        List<User> users = userRepository.findByUsernameContainingIgnoreCase(q);
        for (User u : users) {
            if (!u.getId().equals(currentUser.getId())) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", u.getId());
                result.put("title", u.getFirstName() + " " + u.getLastName());
                result.put("description", "@" + u.getUsername());
                result.put("type", "user");
                result.put("url", "/profile/" + u.getId());
                results.add(result);
            }
        }

        return ResponseEntity.ok(results);
    }
}
