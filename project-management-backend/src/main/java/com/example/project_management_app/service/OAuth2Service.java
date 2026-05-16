package com.example.project_management_app.service;

import com.example.project_management_app.dto.OAuth2UserInfo;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OAuth2Service {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public Map<String, String> processOAuth2User(OAuth2AuthenticationToken authentication) {
        String provider = authentication.getAuthorizedClientRegistrationId();
        OAuth2UserInfo userInfo = extractUserInfo(authentication, provider);

        // Find or create user
        User user = findOrCreateUser(userInfo);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        // Prepare response
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("provider", provider);
        response.put("message", "OAuth2 authentication successful");

        return response;
    }

    private OAuth2UserInfo extractUserInfo(OAuth2AuthenticationToken authentication, String provider) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        userInfo.setProvider(provider);

        switch (provider) {
            case "google":
                userInfo.setEmail((String) attributes.get("email"));
                userInfo.setName((String) attributes.get("name"));
                userInfo.setFirstName((String) attributes.get("given_name"));
                userInfo.setLastName((String) attributes.get("family_name"));
                userInfo.setProviderId((String) attributes.get("sub"));
                userInfo.setAvatarUrl((String) attributes.get("picture"));
                break;

            case "github":
                userInfo.setEmail((String) attributes.get("email"));
                userInfo.setName((String) attributes.get("name"));

                // GitHub doesn't provide firstName/lastName separately
                String name = (String) attributes.get("name");
                if (name != null && name.contains(" ")) {
                    String[] nameParts = name.split(" ", 2);
                    userInfo.setFirstName(nameParts[0]);
                    userInfo.setLastName(nameParts[1]);
                } else {
                    userInfo.setFirstName(name);
                    userInfo.setLastName("");
                }

                userInfo.setProviderId(attributes.get("id").toString());
                userInfo.setAvatarUrl((String) attributes.get("avatar_url"));

                // If email is not public, get it from GitHub API
                if (userInfo.getEmail() == null) {
                    userInfo.setEmail(fetchGitHubEmail(attributes.get("login").toString()));
                }
                break;
        }

        return userInfo;
    }

    private String fetchGitHubEmail(String username) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "ProjectFlow-App");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    "https://api.github.com/users/" + username + "/emails",
                    HttpMethod.GET,
                    entity,
                    Map[].class
            );

            if (response.getBody() != null && response.getBody().length > 0) {
                for (Map emailData : response.getBody()) {
                    Boolean primary = (Boolean) emailData.get("primary");
                    Boolean verified = (Boolean) emailData.get("verified");
                    if (primary != null && primary && verified != null && verified) {
                        return (String) emailData.get("email");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return username + "@github.user";
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info if needed
            user.setLastActivity(LocalDateTime.now());
            if (userInfo.getAvatarUrl() != null) {
                user.setProfilePictureUrl(userInfo.getAvatarUrl());
            }
            return userRepository.save(user);
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(generateUsername(userInfo.getEmail()));
        newUser.setEmail(userInfo.getEmail());
        newUser.setFirstName(userInfo.getFirstName() != null ? userInfo.getFirstName() : userInfo.getName());
        newUser.setLastName(userInfo.getLastName() != null ? userInfo.getLastName() : "");
        newUser.setPassword(UUID.randomUUID().toString()); // Random password for OAuth users
        newUser.setRole(User.Role.ROLE_USER);
        newUser.setProvider(userInfo.getProvider());
        newUser.setProviderId(userInfo.getProviderId());
        newUser.setProfilePictureUrl(userInfo.getAvatarUrl());
        newUser.setLastActivity(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    public String getAuthorizationUrl(String provider) {
        // In a real app, you'd generate this dynamically
        return "http://localhost:8080/oauth2/authorization/" + provider;
    }

    public String buildRedirectUrl(String token, String username, String email) {
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("username", username)
                .queryParam("email", email)
                .build()
                .toUriString();
    }
}