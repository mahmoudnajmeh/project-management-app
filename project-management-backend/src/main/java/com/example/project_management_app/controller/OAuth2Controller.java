package com.example.project_management_app.controller;

import com.example.project_management_app.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    @Autowired
    private OAuth2Service oAuth2Service;

    @GetMapping("/url/{provider}")
    public ResponseEntity<?> getAuthorizationUrl(@PathVariable String provider) {
        try {
            String url = oAuth2Service.getAuthorizationUrl(provider);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate authorization URL");
            return ResponseEntity.badRequest().body(error);
        }
    }
}