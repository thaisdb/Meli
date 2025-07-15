package com.meli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
// Removed unused imports if any, like JsonTypeInfo if not directly used in the constructor
import com.meli.model.User;
import com.meli.model.Consumer; // Keep if you explicitly register subtypes, otherwise can remove
import com.meli.model.Seller;   // Keep if you explicitly register subtypes, otherwise can remove
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Encapsulates business logic related to User management
 * Handles loading/saving/registering/updating and searching users via Jackson
 * Delegate persistance to UserRepository
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
        System.out.println("DEBUG: UserService.loadUsers - Trying to load users from: " + new File(DATA_FILE_PATH).getAbsolutePath());
        try {
            File file = new File(DATA_FILE_PATH);
            if (file.exists()) {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
                users = mapper.readValue(file, listType);
                System.out.println("DEBUG: UserService.loadUsers - Successfully loaded " + users.size() + " users.");
            } else {
                System.out.println("DEBUG: UserService.loadUsers - No user file found. Starting with empty list.");
                users = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("ERROR: UserService.loadUsers - Failed to load users from file: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error info
        }
    }

    private void saveUsers() {
        System.out.println("DEBUG: UserService.saveUsers - Attempting to save " + users.size() + " users to file.");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE_PATH), users);
            System.out.println("DEBUG: UserService.saveUsers - Users saved successfully.");
        } catch (IOException e) {
            System.err.println("ERROR: UserService.saveUsers - Failed to save users to file: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("DEBUG: UserService.registerUser - Registered new user: " + user.getEmail() + " with ID: " + user.getId());
        return user;
    }

    public List<User> getAllUsers() {
        return users;
    }

    public User getUserById(int id) {
        System.out.println("DEBUG: UserService.getUserById - Searching for user with ID: " + id);
        User foundUser = users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
        if (foundUser != null) {
            System.out.println("DEBUG: UserService.getUserById - Found user: " + foundUser.getEmail());
        } else {
            System.out.println("DEBUG: UserService.getUserById - User with ID " + id + " not found.");
        }
        return foundUser;
    }

    public boolean deleteUserById(int id) {
        System.out.println("DEBUG: UserService.deleteUserById - Attempting to delete user with ID: " + id);
        boolean removed = users.removeIf(u -> u.getId() == id);
        if (removed) {
            saveUsers();
            System.out.println("DEBUG: UserService.deleteUserById - User with ID " + id + " removed successfully.");
        } else {
            System.out.println("DEBUG: UserService.deleteUserById - User with ID " + id + " not found for deletion.");
        }
        return removed;
    }

    /*
     * Find user by given email, used in login page
     * @param email
     * @return User
     */
    public User findByEmail(String email) {
        System.out.println("DEBUG: UserService.findByEmail - Searching for email: '" + email + "'");
        User user = users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
        if (user != null) {
            System.out.println("DEBUG: UserService.findByEmail - User found: " + user.getEmail() + " (ID: " + user.getId() + ")");
        } else {
            System.out.println("DEBUG: UserService.findByEmail - User NOT found for email: '" + email + "'");
        }
        return user;
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

    /**
     * Simulates password verification.
     * In a real application, use a secure password encoder (e.g., BCryptPasswordEncoder).
     *
     * @param rawPassword The password entered by the user.
     * @param storedPassword The password retrieved from the database.
     * @return true if passwords match, false otherwise.
     */
    public boolean verifyPassword(String rawPassword, String storedPassword) {
        System.out.println("DEBUG: UserService.verifyPassword - Raw Password: '" + rawPassword + "'");
        System.out.println("DEBUG: UserService.verifyPassword - Stored Password: '" + storedPassword + "'");
        boolean matches = rawPassword.equals(storedPassword);
        System.out.println("DEBUG: UserService.verifyPassword - Passwords Match: " + matches);
        return matches;
    }
}
