package com.example.project_management_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void createTask_Works() {
        Project project = new Project();
        project.setId(1L);
        project.setName("MN ChatBot");

        User user = new User();
        user.setId(1L);
        user.setUsername("Mahmoud");

        Task task = new Task(
                "Planning",
                "Planning for MN ChatBot",
                project,
                user
        );

        assertNotNull(task);
        assertEquals("Planning", task.getTitle());
        assertEquals("Planning for MN ChatBot", task.getDescription());
        assertEquals(project, task.getProject());
        assertEquals(user, task.getAssignedUser());
    }

    @Test
    void setters_Work() {
        Task task = new Task();

        task.setId(2L);
        task.setTitle("Planning for MN ChatBot");
        task.setDescription("MN ChatBot");
        task.setStatus(Task.TaskStatus.DONE);
        task.setPriority(Task.Priority.MEDIUM);

        LocalDateTime due = LocalDateTime.of(2025, 12, 23, 23, 59, 59);
        task.setDueDate(due);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        Project project = new Project();
        project.setId(2L);
        task.setProject(project);

        User user = new User();
        user.setId(1L);
        task.setAssignedUser(user);

        assertEquals(2L, task.getId());
        assertEquals("Planning for MN ChatBot", task.getTitle());
        assertEquals("MN ChatBot", task.getDescription());
        assertEquals(Task.TaskStatus.DONE, task.getStatus());
        assertEquals(Task.Priority.MEDIUM, task.getPriority());
        assertEquals(due, task.getDueDate());
        assertEquals(now, task.getCreatedAt());
        assertEquals(now, task.getUpdatedAt());
        assertEquals(project, task.getProject());
        assertEquals(user, task.getAssignedUser());
    }

    @Test
    void onCreate_SetsDefaults() {
        Task task = new Task();

        task.onCreate();

        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertEquals(Task.TaskStatus.TODO, task.getStatus());
        assertEquals(Task.Priority.MEDIUM, task.getPriority());
    }

    @Test
    void onCreate_KeepsExistingValues() {
        Task task = new Task();
        task.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setPriority(Task.Priority.HIGH);

        task.onCreate();

        assertEquals(Task.TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(Task.Priority.HIGH, task.getPriority());
    }

    @Test
    void onUpdate_UpdatesTimestamp() {
        Task task = new Task();
        task.onCreate();

        LocalDateTime oldUpdated = task.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        task.onUpdate();

        assertNotNull(task.getUpdatedAt());
        assertTrue(task.getUpdatedAt().isAfter(oldUpdated));
    }

    @Test
    void getProjectJson_ReturnsJsonWhenProjectExists() {
        Project project = new Project();
        project.setId(2L);
        project.setName("MN ChatBot");

        Task task = new Task();
        task.setProject(project);

        Task.ProjectJson projectJson = task.getProjectJson();

        assertNotNull(projectJson);
        assertEquals(2L, projectJson.getId());
        assertEquals("MN ChatBot", projectJson.getName());
    }

    @Test
    void getProjectJson_ReturnsNullWhenProjectNull() {
        Task task = new Task();

        assertNull(task.getProjectJson());
    }

    @Test
    void getAssignedUserJson_ReturnsJsonWhenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Mahmoud");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setEmail("mn.de@outlook.com");
        user.setProfilePictureFileName("pic.jpg");
        user.setProfilePictureUrl("/url/pic.jpg");
        user.setProfilePicturePath("./uploads/pic.jpg");
        user.setProfilePictureContentType("image/jpeg");

        Task task = new Task();
        task.setAssignedUser(user);

        Task.UserJson userJson = task.getAssignedUserJson();

        assertNotNull(userJson);
        assertEquals(1L, userJson.getId());
        assertEquals("Mahmoud", userJson.getUsername());
        assertEquals("Mahmoud", userJson.getFirstName());
        assertEquals("Najmeh", userJson.getLastName());
        assertEquals("mn.de@outlook.com", userJson.getEmail());
        assertEquals("pic.jpg", userJson.getProfilePictureFileName());
        assertEquals("/url/pic.jpg", userJson.getProfilePictureUrl());
        assertEquals("./uploads/pic.jpg", userJson.getProfilePicturePath());
        assertEquals("image/jpeg", userJson.getProfilePictureContentType());
    }

    @Test
    void getAssignedUserJson_ReturnsNullWhenUserNull() {
        Task task = new Task();

        assertNull(task.getAssignedUserJson());
    }

    @Test
    void jsonIgnore_OnGetters() throws Exception {
        Method getProjectMethod = Task.class.getMethod("getProject");
        JsonIgnore jsonIgnore = getProjectMethod.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);

        Method getAssignedUserMethod = Task.class.getMethod("getAssignedUser");
        jsonIgnore = getAssignedUserMethod.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);
    }

    @Test
    void jsonProperty_OnJsonMethods() throws Exception {
        Method getProjectJsonMethod = Task.class.getMethod("getProjectJson");
        JsonProperty jsonProperty = getProjectJsonMethod.getAnnotation(JsonProperty.class);
        assertNotNull(jsonProperty);
        assertEquals("project", jsonProperty.value());

        Method getAssignedUserJsonMethod = Task.class.getMethod("getAssignedUserJson");
        jsonProperty = getAssignedUserJsonMethod.getAnnotation(JsonProperty.class);
        assertNotNull(jsonProperty);
        assertEquals("assignedUser", jsonProperty.value());
    }

    @Test
    void notBlank_OnTitle() throws Exception {
        Field titleField = Task.class.getDeclaredField("title");
        NotBlank notBlank = titleField.getAnnotation(NotBlank.class);
        assertNotNull(notBlank);
    }

    @Test
    void column_LengthOnDescription() throws Exception {
        Field descField = Task.class.getDeclaredField("description");
        jakarta.persistence.Column column = descField.getAnnotation(jakarta.persistence.Column.class);
        assertNotNull(column);
        assertEquals(2000, column.length());
    }

    @Test
    void manyToOne_Relationships() throws Exception {
        Field projectField = Task.class.getDeclaredField("project");
        ManyToOne manyToOne = projectField.getAnnotation(ManyToOne.class);
        assertNotNull(manyToOne);
        assertEquals(FetchType.LAZY, manyToOne.fetch());

        JoinColumn joinColumn = projectField.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals("project_id", joinColumn.name());

        Field userField = Task.class.getDeclaredField("assignedUser");
        manyToOne = userField.getAnnotation(ManyToOne.class);
        assertNotNull(manyToOne);

        joinColumn = userField.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals("assigned_user_id", joinColumn.name());
    }

    @Test
    void enum_TaskStatus_Values() {
        assertEquals(4, Task.TaskStatus.values().length);
        assertEquals(Task.TaskStatus.TODO, Task.TaskStatus.valueOf("TODO"));
        assertEquals(Task.TaskStatus.IN_PROGRESS, Task.TaskStatus.valueOf("IN_PROGRESS"));
        assertEquals(Task.TaskStatus.REVIEW, Task.TaskStatus.valueOf("REVIEW"));
        assertEquals(Task.TaskStatus.DONE, Task.TaskStatus.valueOf("DONE"));
    }

    @Test
    void enum_Priority_Values() {
        assertEquals(4, Task.Priority.values().length);
        assertEquals(Task.Priority.LOW, Task.Priority.valueOf("LOW"));
        assertEquals(Task.Priority.MEDIUM, Task.Priority.valueOf("MEDIUM"));
        assertEquals(Task.Priority.HIGH, Task.Priority.valueOf("HIGH"));
        assertEquals(Task.Priority.URGENT, Task.Priority.valueOf("URGENT"));
    }

    @Test
    void defaultConstructor_Works() {
        Task task = new Task();
        assertNotNull(task);
    }

    @Test
    void fromDatabaseData_Task2_Works() {
        Task task = new Task();
        task.setId(2L);
        task.setTitle("Planning for MN ChatBot");
        task.setDescription("MN ChatBot");
        task.setStatus(Task.TaskStatus.DONE);
        task.setPriority(Task.Priority.MEDIUM);
        task.setDueDate(LocalDateTime.of(2025, 12, 23, 23, 59, 59));

        Project project = new Project();
        project.setId(2L);
        project.setName("MN ChatBot");
        task.setProject(project);

        User user = new User();
        user.setId(1L);
        task.setAssignedUser(user);

        assertEquals(2L, task.getId());
        assertEquals("Planning for MN ChatBot", task.getTitle());
        assertEquals(Task.TaskStatus.DONE, task.getStatus());
        assertEquals(Task.Priority.MEDIUM, task.getPriority());
        assertEquals(project, task.getProject());
        assertEquals(user, task.getAssignedUser());
    }

    @Test
    void fromDatabaseData_Task3_Works() {
        Task task = new Task();
        task.setId(3L);
        task.setTitle("Starting with MN ChatBot");
        task.setDescription("MN ChatBot");
        task.setStatus(Task.TaskStatus.DONE);
        task.setPriority(Task.Priority.HIGH);
        task.setDueDate(LocalDateTime.of(2025, 12, 24, 23, 59, 59));

        Project project = new Project();
        project.setId(2L);
        task.setProject(project);

        User user = new User();
        user.setId(1L);
        task.setAssignedUser(user);

        assertEquals(3L, task.getId());
        assertEquals("Starting with MN ChatBot", task.getTitle());
        assertEquals(Task.TaskStatus.DONE, task.getStatus());
        assertEquals(Task.Priority.HIGH, task.getPriority());
    }

    @Test
    void fromDatabaseData_Task5_Works() {
        Task task = new Task();
        task.setId(5L);
        task.setTitle("Enhance the Chat Backend");
        task.setDescription("MN ChatBot");
        task.setStatus(Task.TaskStatus.IN_PROGRESS);
        task.setPriority(Task.Priority.HIGH);
        task.setDueDate(LocalDateTime.of(2026, 1, 30, 23, 59, 59));

        Project project = new Project();
        project.setId(1L);
        task.setProject(project);

        User user = new User();
        user.setId(1L);
        task.setAssignedUser(user);

        assertEquals(5L, task.getId());
        assertEquals("Enhance the Chat Backend", task.getTitle());
        assertEquals(Task.TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(Task.Priority.HIGH, task.getPriority());
    }

    @Test
    void fromDatabaseData_Task6_Works() {
        Task task = new Task();
        task.setId(6L);
        task.setTitle("Authentication and Authorization");
        task.setDescription("MN ChatBot");
        task.setStatus(Task.TaskStatus.TODO);
        task.setPriority(Task.Priority.HIGH);
        task.setDueDate(LocalDateTime.of(2026, 3, 2, 23, 59, 59));

        Project project = new Project();
        project.setId(2L);
        task.setProject(project);

        User user = new User();
        user.setId(1L);
        task.setAssignedUser(user);

        assertEquals(6L, task.getId());
        assertEquals("Authentication and Authorization", task.getTitle());
        assertEquals(Task.TaskStatus.TODO, task.getStatus());
        assertEquals(Task.Priority.HIGH, task.getPriority());
    }

    @Test
    void projectJson_CreatesCorrectly() {
        Task.ProjectJson projectJson = new Task.ProjectJson(2L, "MN ChatBot");

        assertEquals(2L, projectJson.getId());
        assertEquals("MN ChatBot", projectJson.getName());
    }

    @Test
    void userJson_CreatesCorrectly() {
        Task.UserJson userJson = new Task.UserJson(
                1L,
                "Mahmoud",
                "Mahmoud",
                "Najmeh",
                "mn.de@outlook.com",
                "pic.jpg",
                "/url/pic.jpg",
                "./uploads/pic.jpg",
                "image/jpeg"
        );

        assertEquals(1L, userJson.getId());
        assertEquals("Mahmoud", userJson.getUsername());
        assertEquals("Mahmoud", userJson.getFirstName());
        assertEquals("Najmeh", userJson.getLastName());
        assertEquals("mn.de@outlook.com", userJson.getEmail());
        assertEquals("pic.jpg", userJson.getProfilePictureFileName());
        assertEquals("/url/pic.jpg", userJson.getProfilePictureUrl());
        assertEquals("./uploads/pic.jpg", userJson.getProfilePicturePath());
        assertEquals("image/jpeg", userJson.getProfilePictureContentType());
    }
}