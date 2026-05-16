package com.example.project_management_app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void createProject_Works() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Mahmoud");

        Team team = new Team();
        team.setId(1L);
        team.setName("Dev Team");

        Project project = new Project(
                "MN ChatBot",
                "A chatbot platform",
                user,
                team
        );

        assertNotNull(project);
        assertEquals("MN ChatBot", project.getName());
        assertEquals("A chatbot platform", project.getDescription());
        assertEquals(user, project.getCreatedBy());
        assertEquals(team, project.getTeam());
    }

    @Test
    void setters_Work() {
        Project project = new Project();

        project.setId(1L);
        project.setName("MN ChatBot");
        project.setDescription("Chatbot platform");
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);

        LocalDateTime start = LocalDateTime.of(2025, 12, 14, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 1, 23, 59, 59);
        project.setStartDate(start);
        project.setEndDate(end);

        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        User user = new User();
        user.setId(1L);
        project.setCreatedBy(user);

        Team team = new Team();
        team.setId(1L);
        project.setTeam(team);

        assertEquals(1L, project.getId());
        assertEquals("MN ChatBot", project.getName());
        assertEquals("Chatbot platform", project.getDescription());
        assertEquals(Project.ProjectStatus.IN_PROGRESS, project.getStatus());
        assertEquals(start, project.getStartDate());
        assertEquals(end, project.getEndDate());
        assertEquals(now, project.getCreatedAt());
        assertEquals(now, project.getUpdatedAt());
        assertEquals(user, project.getCreatedBy());
        assertEquals(team, project.getTeam());
    }

    @Test
    void onCreate_SetsTimestamps() {
        Project project = new Project();

        project.onCreate();

        assertNotNull(project.getCreatedAt());
        assertNotNull(project.getUpdatedAt());
        assertEquals(Project.ProjectStatus.PLANNED, project.getStatus());
    }

    @Test
    void onCreate_KeepsExistingStatus() {
        Project project = new Project();
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);

        project.onCreate();

        assertEquals(Project.ProjectStatus.IN_PROGRESS, project.getStatus());
    }

    @Test
    void onUpdate_UpdatesTimestamp() {
        Project project = new Project();
        project.onCreate();

        LocalDateTime oldUpdated = project.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }

        project.onUpdate();

        assertNotNull(project.getUpdatedAt());
        assertTrue(project.getUpdatedAt().isAfter(oldUpdated));
    }

    @Test
    void tasks_RelationshipWorks() {
        Project project = new Project();

        List<Task> tasks = new ArrayList<>();
        Task task = new Task();
        task.setId(1L);
        tasks.add(task);

        project.setTasks(tasks);

        assertEquals(1, project.getTasks().size());
        assertEquals(task, project.getTasks().get(0));
    }

    @Test
    void getCreatedByJson_ReturnsJsonWhenUserExists() {
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

        Project project = new Project();
        project.setCreatedBy(user);

        Project.UserJson userJson = project.getCreatedByJson();

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
    void getCreatedByJson_ReturnsNullWhenUserNull() {
        Project project = new Project();

        assertNull(project.getCreatedByJson());
    }

    @Test
    void jsonIgnore_OnGetCreatedBy() throws Exception {
        Method method = Project.class.getMethod("getCreatedBy");
        JsonIgnore jsonIgnore = method.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);

        Field tasksField = Project.class.getDeclaredField("tasks");
        jsonIgnore = tasksField.getAnnotation(JsonIgnore.class);
        assertNotNull(jsonIgnore);
    }

    @Test
    void jsonProperty_OnGetCreatedByJson() throws Exception {
        Method method = Project.class.getMethod("getCreatedByJson");
        JsonProperty jsonProperty = method.getAnnotation(JsonProperty.class);
        assertNotNull(jsonProperty);
        assertEquals("createdBy", jsonProperty.value());
    }

    @Test
    void notBlank_OnName() throws Exception {
        Field nameField = Project.class.getDeclaredField("name");
        NotBlank notBlank = nameField.getAnnotation(NotBlank.class);
        assertNotNull(notBlank);
    }

    @Test
    void column_LengthOnDescription() throws Exception {
        Field descField = Project.class.getDeclaredField("description");
        jakarta.persistence.Column column = descField.getAnnotation(jakarta.persistence.Column.class);
        assertNotNull(column);
        assertEquals(2000, column.length());
    }

    @Test
    void manyToOne_Relationships() throws Exception {
        Field createdByField = Project.class.getDeclaredField("createdBy");
        ManyToOne manyToOne = createdByField.getAnnotation(ManyToOne.class);
        assertNotNull(manyToOne);
        assertEquals(FetchType.LAZY, manyToOne.fetch());

        JoinColumn joinColumn = createdByField.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals("created_by", joinColumn.name());

        Field teamField = Project.class.getDeclaredField("team");
        manyToOne = teamField.getAnnotation(ManyToOne.class);
        assertNotNull(manyToOne);

        joinColumn = teamField.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals("team_id", joinColumn.name());
    }

    @Test
    void oneToMany_OnTasks() throws Exception {
        Field tasksField = Project.class.getDeclaredField("tasks");
        OneToMany oneToMany = tasksField.getAnnotation(OneToMany.class);
        assertNotNull(oneToMany);
        assertEquals("project", oneToMany.mappedBy());
        assertEquals(CascadeType.ALL, oneToMany.cascade()[0]);
        assertEquals(FetchType.LAZY, oneToMany.fetch());
    }

    @Test
    void enum_Values() {
        assertEquals(4, Project.ProjectStatus.values().length);
        assertEquals(Project.ProjectStatus.PLANNED, Project.ProjectStatus.valueOf("PLANNED"));
        assertEquals(Project.ProjectStatus.IN_PROGRESS, Project.ProjectStatus.valueOf("IN_PROGRESS"));
        assertEquals(Project.ProjectStatus.COMPLETED, Project.ProjectStatus.valueOf("COMPLETED"));
        assertEquals(Project.ProjectStatus.CANCELLED, Project.ProjectStatus.valueOf("CANCELLED"));
    }

    @Test
    void defaultConstructor_Works() {
        Project project = new Project();
        assertNotNull(project);
    }

    @Test
    void fromDatabaseData_Works() {
        Project project = new Project();
        project.setId(1L);
        project.setName("MN ChatBot");
        project.setDescription("MN ChatBot is a modern, intelligent conversational platform...");
        project.setStatus(Project.ProjectStatus.IN_PROGRESS);
        project.setStartDate(LocalDateTime.of(2025, 12, 14, 0, 0));
        project.setEndDate(LocalDateTime.of(2026, 6, 1, 23, 59, 59));

        User user = new User();
        user.setId(2L);
        project.setCreatedBy(user);

        assertEquals(1L, project.getId());
        assertEquals("MN ChatBot", project.getName());
        assertEquals(Project.ProjectStatus.IN_PROGRESS, project.getStatus());
        assertEquals(user, project.getCreatedBy());
    }

    @Test
    void userJson_CreatesCorrectly() {
        Project.UserJson userJson = new Project.UserJson(
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