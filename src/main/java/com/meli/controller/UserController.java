package com.meli.controller;

import com.meli.model.User;
import com.meli.service.UserService;
import com.meli.dto.LoginRequestDTO; // Certifique-se de que este DTO está correto

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Importar Optional

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
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (user.getName() == null || user.getName().isEmpty() ||
            user.getPassword() == null || user.getPassword().isEmpty() ||
            user.getEmail() == null || user.getEmail().isEmpty() ||
            user.getCpf() == null || user.getCpf().isEmpty() ||
            user.getAddress() == null || user.getAddress().isEmpty() ||
            user.getType() == null || user.getType().isEmpty()) { // Usar getType() para o tipo de usuário
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Todos os campos (nome, email, cpf, senha, endereço, tipo) são obrigatórios.");
        }

        Optional<User> createdUser = userService.registerUser(user); // Agora retorna Optional
        if (createdUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado. Por favor, use outro email.");
        }
    }

    /**
     * Authenticate user and return user details for redirection.
     * This method will handle the login logic.
     */
    @PostMapping(value = "/login", consumes = "application/json") 
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        Optional<User> foundUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (foundUser.isPresent()) {
            return ResponseEntity.ok(foundUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Credenciais inválidas. Verifique seu email e senha.");
        }
    }   

    /**
     * Update user by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User userDetails) {
        if (userDetails.getName() == null || userDetails.getName().isEmpty() ||
            userDetails.getEmail() == null || userDetails.getEmail().isEmpty() ||
            userDetails.getCpf() == null || userDetails.getCpf().isEmpty() ||
            userDetails.getAddress() == null || userDetails.getAddress().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nome, email, cpf e endereço são obrigatórios para atualização.");
        }

        Optional<User> updatedUser = userService.updateUser(userDetails); // Agora retorna Optional
        if (updatedUser.isPresent()) {
            return ResponseEntity.ok(updatedUser.get());
        } else {
            if (userService.getUserById(id) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário com ID " + id + " não encontrado para atualização.");
            } else if (userService.findByEmail(userDetails.getEmail()) != null && 
                       userService.getUserById(id) != null && // Garante que o usuário original existe
                       !userService.getUserById(id).getEmail().equalsIgnoreCase(userDetails.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado para outro usuário. Por favor, use outro email.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha na atualização do usuário. Verifique os dados e tente novamente.");
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
