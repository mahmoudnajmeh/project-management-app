package com.example.project_management_app.service;

import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TaskRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.MessageRepository;
import com.example.project_management_app.repository.NotificationRepository;
import com.example.project_management_app.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomUserDetailsService{

}