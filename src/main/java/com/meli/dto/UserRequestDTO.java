package com.meli.dto;

/*
 * Data Transfer Object used to carry data between processes
 * Data -> Backend
 * Controler -> Service layer
 * Shaped for the resquest, it helps decouple data model from external APIs
 */
public class UserRequestDTO {
    private String name;
    private String email;
    private String cpf;
    private String password;
    private String address;
    private String role; // "consumer" or "seller"

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
