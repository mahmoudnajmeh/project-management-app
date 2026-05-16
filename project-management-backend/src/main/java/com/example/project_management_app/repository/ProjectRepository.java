package com.example.project_management_app.repository;

import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.createdBy WHERE p.createdBy = :user")
    List<Project> findByCreatedBy(@Param("user") User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.createdBy")
    List<Project> findAll();

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.tasks t " +
            "WHERE t.assignedUser = :user")
    List<Project> findByTasksAssignedUser(@Param("user") User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.createdBy WHERE p.team = :team")
    List<Project> findByTeam(@Param("team") Team team);

    List<Project> findByNameContainingIgnoreCase(String name);
}