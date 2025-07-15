package com.meli.controller;

import com.meli.model.User;
import com.meli.service.UserService;
import com.meli.dto.LoginRequestDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Handles /signup, /login, etc.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor injection (required for Spring Boot)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get all users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Usuário com ID " + id + " não foi encontrado.");
        }
    }

    /**
     * Register new user (Consumer or Seller depending on JSON type)
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User created = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Authenticate user and return user details for redirection.
     * This method will handle the login logic.
     */
    @PostMapping(value = "/login", consumes = "application/json") // Added explicit consumes
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());

        if (user != null && userService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Credenciais inválidas.");
        }
    }

    /**
     * Delete user by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable int id) {
        boolean removed = userService.deleteUserById(id);
        if (removed) {
            return ResponseEntity.ok("Usuário removido com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Usuário com ID " + id + " não encontrado.");
        }
    }

    /**
     * Find user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Usuário com email " + email + " não encontrado.");
        }
    }
}
