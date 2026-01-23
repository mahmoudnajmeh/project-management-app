package com.example.project_management_app.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.crypto.SecretKey;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.invitation.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${app.invitation.secret:your-invitation-secret-key-change-this-in-production}")
    private String invitationSecret;

    @Value("${app.invitation.expiration-hours:72}")
    private int expirationHours;

    public void sendInvitationEmail(String toEmail, String role, String invitedBy, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Invitation to Join Project Management App");

        Map<String, Object> variables = new HashMap<>();
        variables.put("email", toEmail);
        variables.put("role", role);
        variables.put("invitedBy", invitedBy);
        variables.put("invitationLink", baseUrl + "/register?token=" + token);

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("invitation-email", context);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public String generateInvitationToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(invitationSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationHours * 3600000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateInvitationToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(invitationSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(invitationSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid invitation token");
        }
    }
}