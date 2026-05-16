package com.example.project_management_app.controller;

import com.example.project_management_app.entity.User;
import com.example.project_management_app.service.AIAssistantService;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIAssistantService aiAssistantService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @PostMapping("/project-summary/{projectId}")
    public ResponseEntity<?> getProjectSummary(@PathVariable Long projectId) {
        try {
            String summary = aiAssistantService.generateProjectSummary(projectId);
            Map<String, String> response = new HashMap<>();
            response.put("summary", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "AI service unavailable: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/task-priority")
    public ResponseEntity<?> getTaskPriority(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String description = request.get("description");
            String result = aiAssistantService.recommendTaskPriority(title, description);
            Map<String, String> response = new HashMap<>();
            response.put("recommendation", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "AI service unavailable: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> request) {
        try {
            User currentUser = accountService.getCurrentUser();
            String question = request.get("question");
            String answer = aiAssistantService.answerUserQuestion(currentUser.getId(), question);
            Map<String, String> response = new HashMap<>();
            response.put("answer", answer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "AI service unavailable: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/suggest-members")
    public ResponseEntity<?> suggestMembers(@RequestBody Map<String, String> request) {
        try {
            User currentUser = accountService.getCurrentUser();
            String taskDescription = request.get("description");
            List<User> teamMembers = userService.getUsersInSameTeam(currentUser);
            String suggestions = aiAssistantService.suggestTeamMembers(taskDescription, teamMembers);
            Map<String, String> response = new HashMap<>();
            response.put("suggestions", suggestions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "AI service unavailable: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}