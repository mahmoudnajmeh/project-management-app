package com.example.project_management_app.entity;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    @Test
    void createTeam_Works() {
        User user = new User();
        user.setId(1L);
        user.setUsername("Mahmoud");

        Team team = new Team(
                "Development Team",
                "Core development team",
                user
        );

        assertNotNull(team);
        assertEquals("Development Team", team.getName());
        assertEquals("Core development team", team.getDescription());
        assertEquals(user, team.getCreatedBy());
    }

    @Test
    void setters_Work() {
        Team team = new Team();

        team.setId(1L);
        team.setName("Development Team");
        team.setDescription("Core development team");

        User user = new User();
        user.setId(1L);
        team.setCreatedBy(user);

        LocalDateTime now = LocalDateTime.now();
        team.setCreatedAt(now);

        List<User> members = new ArrayList<>();
        User member1 = new User();
        member1.setId(1L);
        User member2 = new User();
        member2.setId(2L);
        members.add(member1);
        members.add(member2);
        team.setMembers(members);

        assertEquals(1L, team.getId());
        assertEquals("Development Team", team.getName());
        assertEquals("Core development team", team.getDescription());
        assertEquals(user, team.getCreatedBy());
        assertEquals(now, team.getCreatedAt());
        assertEquals(2, team.getMembers().size());
        assertTrue(team.getMembers().contains(member1));
        assertTrue(team.getMembers().contains(member2));
    }

    @Test
    void onCreate_SetsTimestamp() {
        Team team = new Team();

        team.onCreate();

        assertNotNull(team.getCreatedAt());
        assertTrue(team.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void defaultConstructor_Works() {
        Team team = new Team();
        assertNotNull(team);
    }

    @Test
    void members_ListCanBeEmpty() {
        Team team = new Team();
        team.setMembers(new ArrayList<>());

        assertNotNull(team.getMembers());
        assertTrue(team.getMembers().isEmpty());
    }

    @Test
    void members_CanBeNull() {
        Team team = new Team();

        assertNull(team.getMembers());
    }

    @Test
    void addMember_Works() {
        Team team = new Team();
        List<User> members = new ArrayList<>();

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        members.add(user1);
        members.add(user2);
        team.setMembers(members);

        assertEquals(2, team.getMembers().size());
        assertEquals(user1, team.getMembers().get(0));
        assertEquals(user2, team.getMembers().get(1));
    }

    @Test
    void removeMember_Works() {
        Team team = new Team();
        List<User> members = new ArrayList<>();

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        members.add(user1);
        members.add(user2);
        team.setMembers(members);

        members.remove(user1);
        team.setMembers(members);

        assertEquals(1, team.getMembers().size());
        assertEquals(user2, team.getMembers().get(0));
    }

    @Test
    void createdBy_CanBeNull() {
        Team team = new Team();

        assertNull(team.getCreatedBy());
    }

    @Test
    void manyToOne_Annotations() throws Exception {
        Field createdByField = Team.class.getDeclaredField("createdBy");
        ManyToOne manyToOne = createdByField.getAnnotation(ManyToOne.class);
        assertNotNull(manyToOne);
        assertEquals(FetchType.LAZY, manyToOne.fetch());

        JoinColumn joinColumn = createdByField.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals("created_by", joinColumn.name());
    }

    @Test
    void manyToMany_Annotations() throws Exception {
        Field membersField = Team.class.getDeclaredField("members");
        ManyToMany manyToMany = membersField.getAnnotation(ManyToMany.class);
        assertNotNull(manyToMany);
        assertEquals(FetchType.LAZY, manyToMany.fetch());

        JoinTable joinTable = membersField.getAnnotation(JoinTable.class);
        assertNotNull(joinTable);
        assertEquals("team_members", joinTable.name());

        JoinColumn[] joinColumns = joinTable.joinColumns();
        assertEquals(1, joinColumns.length);
        assertEquals("team_id", joinColumns[0].name());

        JoinColumn[] inverseJoinColumns = joinTable.inverseJoinColumns();
        assertEquals(1, inverseJoinColumns.length);
        assertEquals("user_id", inverseJoinColumns[0].name());
    }

    @Test
    void table_Annotation() {
        Table table = Team.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("teams", table.name());
    }

    @Test
    void id_GeneratedValue() throws Exception {
        Field idField = Team.class.getDeclaredField("id");
        Id id = idField.getAnnotation(Id.class);
        assertNotNull(id);

        GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());
    }

    @Test
    void fromDatabaseData_Works() {
        Team team = new Team();
        team.setId(1L);
        team.setName("Development Team");
        team.setDescription("Core development team");

        User user = new User();
        user.setId(1L);
        team.setCreatedBy(user);

        List<User> members = new ArrayList<>();
        User member1 = new User();
        member1.setId(1L);
        User member2 = new User();
        member2.setId(2L);
        members.add(member1);
        members.add(member2);
        team.setMembers(members);

        LocalDateTime now = LocalDateTime.now();
        team.setCreatedAt(now);

        assertEquals(1L, team.getId());
        assertEquals("Development Team", team.getName());
        assertEquals("Core development team", team.getDescription());
        assertEquals(user, team.getCreatedBy());
        assertEquals(2, team.getMembers().size());
        assertEquals(now, team.getCreatedAt());
    }

    @Test
    void equalsAndHashCode_NotOverridden() {
        Team team1 = new Team();
        team1.setId(1L);
        team1.setName("Team A");

        Team team2 = new Team();
        team2.setId(1L);
        team2.setName("Team A");

        assertNotEquals(team1, team2);
        assertNotEquals(team1.hashCode(), team2.hashCode());
    }

    @Test
    void toString_ReturnsDefaultImplementation() {
        Team team = new Team();
        team.setId(1L);
        team.setName("Team A");

        String toString = team.toString();

        assertNotNull(toString);
        assertTrue(toString.startsWith("com.example.project_management_app.entity.Team@"));
    }

    @Test
    void constructor_WithNullValues() {
        Team team = new Team(null, null, null);

        assertNull(team.getName());
        assertNull(team.getDescription());
        assertNull(team.getCreatedBy());
    }
}