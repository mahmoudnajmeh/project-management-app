package com.example.project_management_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createUser_Works() {
        User user = new User(
                "Mahmoud",
                "mn.de@outlook.com",
                "password123",
                "Mahmoud",
                "Najmeh",
                User.Role.ROLE_USER
        );

        assertNotNull(user);
        assertEquals("Mahmoud", user.getUsername());
        assertEquals("mn.de@outlook.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Mahmoud", user.getFirstName());
        assertEquals("Najmeh", user.getLastName());
        assertEquals(User.Role.ROLE_USER, user.getRole());
        assertNotNull(user.getLastActivity());
    }

    @Test
    void setters_Work() {
        User user = new User();

        user.setId(1L);
        user.setUsername("Mahmoud");
        user.setEmail("mn.de@outlook.com");
        user.setPassword("pass123");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setRole(User.Role.ROLE_ADMIN);

        LocalDateTime now = LocalDateTime.now();
        user.setLastActivity(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user.setProfilePictureFileName("pic.jpg");
        user.setProfilePictureUrl("/url/pic.jpg");
        user.setProfilePicturePath("./uploads/pic.jpg");
        user.setProfilePictureContentType("image/jpeg");
        user.setProfilePictureSize(1024L);

        user.setProvider("google");
        user.setProviderId("12345");

        assertEquals(1L, user.getId());
        assertEquals("Mahmoud", user.getUsername());
        assertEquals("mn.de@outlook.com", user.getEmail());
        assertEquals("pass123", user.getPassword());
        assertEquals("Mahmoud", user.getFirstName());
        assertEquals("Najmeh", user.getLastName());
        assertEquals(User.Role.ROLE_ADMIN, user.getRole());
        assertEquals(now, user.getLastActivity());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals("pic.jpg", user.getProfilePictureFileName());
        assertEquals("/url/pic.jpg", user.getProfilePictureUrl());
        assertEquals("./uploads/pic.jpg", user.getProfilePicturePath());
        assertEquals("image/jpeg", user.getProfilePictureContentType());
        assertEquals(1024L, user.getProfilePictureSize());
        assertEquals("google", user.getProvider());
        assertEquals("12345", user.getProviderId());
    }

    @Test
    void onCreate_SetsTimestamps() {
        User user = new User();

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getLastActivity());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void onUpdate_UpdatesTimestamp() {
        User user = new User();
        user.onCreate();

        LocalDateTime oldUpdated = user.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(oldUpdated));
    }

    @Test
    void relationships_Work() {
        User user = new User();

        List<Project> projects = new ArrayList<>();
        Project project = new Project();
        projects.add(project);
        user.setCreatedProjects(projects);

        List<Task> tasks = new ArrayList<>();
        Task task = new Task();
        tasks.add(task);
        user.setAssignedTasks(tasks);

        assertEquals(1, user.getCreatedProjects().size());
        assertEquals(project, user.getCreatedProjects().get(0));
        assertEquals(1, user.getAssignedTasks().size());
        assertEquals(task, user.getAssignedTasks().get(0));
    }

    @Test
    void jsonIgnore_AnnotationsPresent() throws Exception {
        Field passwordField = User.class.getDeclaredField("password");
        JsonIgnore jsonIgnore = passwordField.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);

        Field createdProjectsField = User.class.getDeclaredField("createdProjects");
        jsonIgnore = createdProjectsField.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);

        Field assignedTasksField = User.class.getDeclaredField("assignedTasks");
        jsonIgnore = assignedTasksField.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);
    }

    @Test
    void notBlank_AnnotationsPresent() throws Exception {
        Field usernameField = User.class.getDeclaredField("username");
        NotBlank notBlank = usernameField.getAnnotation(NotBlank.class);
        assertNotNull(notBlank);

        Field emailField = User.class.getDeclaredField("email");
        notBlank = emailField.getAnnotation(NotBlank.class);
        assertNotNull(notBlank);

        Field passwordField = User.class.getDeclaredField("password");
        notBlank = passwordField.getAnnotation(NotBlank.class);
        assertNotNull(notBlank);
    }

    @Test
    void email_AnnotationPresent() throws Exception {
        Field emailField = User.class.getDeclaredField("email");
        Email email = emailField.getAnnotation(Email.class);
        assertNotNull(email);
    }

    @Test
    void column_UniqueConstraints() throws Exception {
        Field usernameField = User.class.getDeclaredField("username");
        jakarta.persistence.Column column = usernameField.getAnnotation(jakarta.persistence.Column.class);
        assertTrue(column.unique());

        Field emailField = User.class.getDeclaredField("email");
        column = emailField.getAnnotation(jakarta.persistence.Column.class);
        assertTrue(column.unique());
    }

    @Test
    void enum_Values() {
        assertEquals(2, User.Role.values().length);
        assertEquals(User.Role.ROLE_USER, User.Role.valueOf("ROLE_USER"));
        assertEquals(User.Role.ROLE_ADMIN, User.Role.valueOf("ROLE_ADMIN"));
    }

    @Test
    void defaultConstructor_Works() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    void fromDatabaseData_Works() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Mahmoud");
        user.setEmail("mn.de@outlook.com");
        user.setPassword("$2a$10$GMSdL3Sq7mLftnaa1gsS9.a.H4g1q4mwo/NIR8jOsYowiI5XYUon2");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setRole(User.Role.ROLE_USER);
        user.setProfilePictureFileName("user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg");
        user.setProfilePictureUrl("/api/users/profile-picture/user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg");
        user.setProfilePicturePath("./uploads/profile-pictures/user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg");
        user.setProfilePictureContentType("image/jpeg");
        user.setProfilePictureSize(253741L);
        user.setProvider("");
        user.setProviderId("");

        assertEquals(1L, user.getId());
        assertEquals("Mahmoud", user.getUsername());
        assertEquals("mn.de@outlook.com", user.getEmail());
        assertEquals("Mahmoud", user.getFirstName());
        assertEquals("Najmeh", user.getLastName());
        assertEquals(User.Role.ROLE_USER, user.getRole());
        assertEquals("user_1_130ef6cc-9ad1-4845-aa11-af8376ce67e6.jpg", user.getProfilePictureFileName());
    }

    @Test
    void fromAnotherUser_Works() {
        User user = new User();
        user.setId(2L);
        user.setUsername("Katya");
        user.setEmail("mamocool3@gmail.com");
        user.setPassword("$2a$10$7sVZHwcbWKglvTCCCFfzcOLbM5Y1ihnTy7TQNSGe1gle6Epxv35ZC");
        user.setFirstName("Katya");
        user.setLastName("Otto");
        user.setRole(User.Role.ROLE_USER);
        user.setProfilePictureFileName("user_2_1adc66c7-7396-4eea-8202-fa037dd703fc.jpg");
        user.setProfilePictureUrl("/api/users/profile-picture/user_2_1adc66c7-7396-4eea-8202-fa037dd703fc.jpg");
        user.setProfilePicturePath("./uploads/profile-pictures/user_2_1adc66c7-7396-4eea-8202-fa037dd703fc.jpg");
        user.setProfilePictureContentType("image/jpeg");
        user.setProfilePictureSize(263274L);
        user.setProvider("lala");
        user.setProviderId("");

        assertEquals(2L, user.getId());
        assertEquals("Katya", user.getUsername());
        assertEquals("mamocool3@gmail.com", user.getEmail());
        assertEquals("Katya", user.getFirstName());
        assertEquals("Otto", user.getLastName());
        assertEquals(User.Role.ROLE_USER, user.getRole());
        assertEquals("user_2_1adc66c7-7396-4eea-8202-fa037dd703fc.jpg", user.getProfilePictureFileName());
        assertEquals("lala", user.getProvider());
    }
}