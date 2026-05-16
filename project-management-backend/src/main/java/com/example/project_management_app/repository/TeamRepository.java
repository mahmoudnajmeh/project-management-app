package com.example.project_management_app.repository;

import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    List<Team> findByMemberId(@Param("userId") Long userId);

    @Query("SELECT t FROM Team t WHERE t.createdBy.id = :userId")
    List<Team> findByCreatedBy(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM Team t JOIN t.members u WHERE t.id = :teamId")
    List<User> findTeamMembers(@Param("teamId") Long teamId);

    List<Team> findByNameContainingIgnoreCase(String name);
}