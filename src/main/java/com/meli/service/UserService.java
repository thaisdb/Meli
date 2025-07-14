package com.meli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.meli.model.User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Encapsulates business logic related to User management
 * Handles loading/saving/registering/updateting and searching users via Jackson
 * 
 */
@Service
public class UserService {
    private final String DATA_FILE_PATH = "data/users.json";
    private List<User> users = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public UserService() {
        loadUsers();
    }

    private void loadUsers() {
        System.out.println("Trying to load users from: " + new File(DATA_FILE_PATH).getAbsolutePath());
        try {
            File file = new File(DATA_FILE_PATH);
            if (file.exists()) {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
                users = mapper.readValue(file, listType);
            } else {
                System.out.println("No user file found. Starting with empty list.");
                users = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Failed to load users from file: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE_PATH), users);
        } catch (IOException e) {
            System.err.println("Failed to save users to file: " + e.getMessage());
        }
    }

    /**
     * Register a new user (assigns a unique ID and saves)
     */
    public User registerUser(User user) {
        int newId = generateNextId();
        user.setId(newId);
        users.add(user);
        saveUsers();
        return user;
    }

    public List<User> getAllUsers() {
        return users;
    }

    public User getUserById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public boolean deleteUserById(int id) {
        boolean removed = users.removeIf(u -> u.getId() == id);
        if (removed) saveUsers();
        return removed;
    }

    public User findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    /*
     * Generate next Id based on last existing user record
     * @return unique Id
     */
    private int generateNextId() {
        return users.stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
    }
}
