package com.example.project_management_app.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload Map<String, Object> heartbeat) {
        // Just acknowledge heartbeat - no response needed
        System.out.println("Heartbeat received from user: " + heartbeat.get("userId"));
    }

    @MessageMapping("/ping")
    public void handlePing(@Payload Map<String, Object> ping) {
        // Just acknowledge ping
        System.out.println("Ping received from user: " + ping.get("userId"));
    }
}