package com.example.project_management_app.service;

import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIAssistantService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${ai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Groq API endpoint (free and fast)
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private String callAI(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return generateMockResponse(prompt);
        }

        try {
            // Prepare request body for Groq API
            Map<String, Object> requestBody = new HashMap<>();
            // Updated to use current model - llama3-70b-8192 is deprecated
            requestBody.put("model", "llama-3.3-70b-versatile");
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful project management assistant."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            // Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(requestBody, headers);

            // Make API call
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    GROQ_API_URL,
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Extract the response content
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "Unable to get response from AI service.";
        } catch (Exception e) {
            System.err.println("AI API error: " + e.getMessage());
            return generateMockResponse(prompt);
        }
    }

    private String generateMockResponse(String prompt) {
        if (prompt.contains("project status summary")) {
            int total = extractNumber(prompt, "Total Tasks: (\\d+)");
            int completed = extractNumber(prompt, "Completed Tasks: (\\d+)");
            int progress = total > 0 ? (completed * 100 / total) : 0;

            return String.format("""
                📊 **Project Summary**
                
                Progress: %d%% complete (%d/%d tasks)
                
                ✅ What's going well: The team is making steady progress on active tasks.
                
                ⚠️ Attention needed: Review overdue tasks and adjust timelines if necessary.
                
                💡 Recommendation: Focus on completing high-priority tasks and communicate any blockers.
                """, progress, completed, total);
        }

        if (prompt.contains("recommend a priority level")) {
            return """
                {
                  "priority": "MEDIUM",
                  "reason": "This task appears to be important but not urgently time-sensitive. Consider team capacity and deadlines."
                }
                """;
        }

        if (prompt.contains("AI assistant for a project management app")) {
            return """
                👋 Hi! I'm your ProjectFlow AI Assistant. I can help you with:
                
                • 📋 Viewing task summaries and project progress
                • 🎯 Suggesting task priorities
                • 👥 Recommending team members for tasks
                • 📊 Generating project reports
                
                Try asking: "Show me my pending tasks" or "What's the status of my projects?"
                """;
        }

        if (prompt.contains("suggest which team member")) {
            return """
                👥 **Team Member Suggestion**
                
                Based on the task requirements, I would recommend:
                
                1. Assign to a team member with relevant expertise
                2. Check current workload before assigning
                3. Consider skill alignment with task requirements
                
                Would you like me to help you assign this task to someone specific?
                """;
        }

        return "I'm here to help! Ask me about task priorities, project summaries, or team assignments.";
    }

    private int extractNumber(String text, String pattern) {
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public String generateProjectSummary(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return "Project not found";
        }

        List<Task> tasks = taskRepository.findByProject(project);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
        long overdueTasks = tasks.stream().filter(t ->
                t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now())
                        && t.getStatus() != Task.TaskStatus.DONE
        ).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();

        String prompt = String.format("""
            Generate a concise project status summary for:
            
            Project: %s
            Description: %s
            Total Tasks: %d
            Completed Tasks: %d
            In Progress Tasks: %d
            Overdue Tasks: %d
            
            Provide a professional summary with recommendations. Use emojis for visual appeal.
            Keep it under 200 words.
            """,
                project.getName(),
                project.getDescription() != null ? project.getDescription() : "No description",
                totalTasks,
                completedTasks,
                inProgressTasks,
                overdueTasks
        );

        return callAI(prompt);
    }

    public String recommendTaskPriority(String title, String description) {
        String prompt = String.format("""
            Based on the following task, recommend a priority level (LOW, MEDIUM, HIGH, or URGENT).
            Also provide a brief explanation why.
            
            Task Title: %s
            Task Description: %s
            
            Respond in JSON format: {"priority": "LEVEL", "reason": "explanation"}
            """, title, description != null ? description : "");

        return callAI(prompt);
    }

    public String answerUserQuestion(Long userId, String question) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found";
        }

        List<Task> userTasks = taskRepository.findByAssignedUser(user);
        List<Project> userProjects = projectRepository.findByCreatedBy(user);

        String tasksContext = userTasks.stream()
                .limit(10)
                .map(t -> String.format("- Task: %s | Status: %s | Due: %s",
                        t.getTitle(), t.getStatus(), t.getDueDate() != null ? t.getDueDate().toString() : "No due date"))
                .collect(Collectors.joining("\n"));

        String projectsContext = userProjects.stream()
                .limit(5)
                .map(p -> String.format("- Project: %s | Status: %s", p.getName(), p.getStatus()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            You are an AI assistant for a project management app called ProjectFlow.
            The user is %s %s.
            
            Their tasks:
            %s
            
            Their projects:
            %s
            
            User question: %s
            
            Answer helpfully and concisely. Use emojis. Keep response under 150 words.
            """,
                user.getFirstName() != null ? user.getFirstName() : "User",
                user.getLastName() != null ? user.getLastName() : "",
                tasksContext.isEmpty() ? "No tasks assigned" : tasksContext,
                projectsContext.isEmpty() ? "No projects" : projectsContext,
                question
        );

        return callAI(prompt);
    }

    public String suggestTeamMembers(String taskDescription, List<User> teamMembers) {
        String membersContext = teamMembers.stream()
                .limit(10)
                .map(m -> String.format("- %s %s (Role: %s)",
                        m.getFirstName() != null ? m.getFirstName() : "",
                        m.getLastName() != null ? m.getLastName() : "",
                        m.getRole()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            Based on the task description, suggest which team member(s) would be best suited.
            
            Task: %s
            
            Available team members:
            %s
            
            Suggest 1-2 members and explain why. Be specific about skills or expertise.
            """, taskDescription, membersContext.isEmpty() ? "No team members available" : membersContext);

        return callAI(prompt);
    }

    public String generateNotificationMessage(String eventType, Map<String, Object> context) {
        String prompt = String.format("""
            Generate a friendly, professional notification message for this event:
            
            Event Type: %s
            Context: %s
            
            Keep it concise, actionable, and use emojis.
            """, eventType, context.toString());

        return callAI(prompt);
    }
}