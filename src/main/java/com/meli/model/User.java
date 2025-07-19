package com.meli.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
 * Base class for all user types (Consumer, Seller).
 * Uses Jackson annotations for polymorphic deserialization based on the 'type' field.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Consumer.class, name = "consumer"),
    @JsonSubTypes.Type(value = Seller.class, name = "seller")
})
public abstract class User {
    private int id;
    private String name;
    private String email;
    private String cpf;
    private String password; // In a real app, this would be hashed
    private String address;

    // No-argument constructor is essential for Jackson deserialization
    public User() {
    }

    // Parameterized constructor for convenience
    public User(String name, String email, String cpf, String password, String address) {
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.address = address;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Abstract method to be implemented by subclasses to return their type
    public abstract String getType(); // THIS IS THE METHOD CAUSING THE ERROR
}
