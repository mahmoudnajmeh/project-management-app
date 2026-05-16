package com.example.project_management_app.repository;

import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN FETCH t.project " +
            "LEFT JOIN FETCH t.assignedUser " +
            "WHERE t.assignedUser = :user")
    List<Task> findByAssignedUser(@Param("user") User user);

    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN FETCH t.project " +
            "LEFT JOIN FETCH t.assignedUser " +
            "WHERE t.project = :project")
    List<Task> findByProject(@Param("project") Project project);

    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN FETCH t.project p " +
            "LEFT JOIN FETCH t.assignedUser " +
            "WHERE p.team IN :teams")
    List<Task> findByProjectTeamIn(@Param("teams") List<com.example.project_management_app.entity.Team> teams);

    @Override
    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN FETCH t.project " +
            "LEFT JOIN FETCH t.assignedUser")
    List<Task> findAll();

    List<Task> findByTitleContainingIgnoreCase(String title);
}