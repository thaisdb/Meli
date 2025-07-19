package com.meli.dto; // Make sure this matches your project structure

public class LoginRequestDTO {
    private String email;
    private String password;

    // No-argument constructor is essential for JSON deserialization by Jackson
    public LoginRequestDTO() {
    }

    // Parameterized constructor for convenience
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters for all fields are required by Jackson
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
