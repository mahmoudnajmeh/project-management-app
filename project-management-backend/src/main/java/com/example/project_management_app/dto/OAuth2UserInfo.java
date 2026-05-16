package com.example.project_management_app.dto;

public class OAuth2UserInfo {
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String provider;
    private String providerId;
    private String avatarUrl;

    public OAuth2UserInfo() {}

    public OAuth2UserInfo(String email, String name, String firstName, String lastName,
                          String provider, String providerId, String avatarUrl) {
        this.email = email;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.provider = provider;
        this.providerId = providerId;
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}