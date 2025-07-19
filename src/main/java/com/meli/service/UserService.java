package com.meli.service;

import com.meli.model.User;
import com.meli.model.Consumer;
import com.meli.model.Seller;
import com.meli.repository.UserRepository; // Importar UserRepository
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    // REMOVIDO: private final String DATA_FILE_PATH = "data/users.json";
    // REMOVIDO: private List<User> users = new ArrayList<>();
    // REMOVIDO: private final ObjectMapper mapper = new ObjectMapper();

    private final UserRepository userRepository; // Injetar UserRepository

    public UserService(UserRepository userRepository) { // Construtor com injeção
        this.userRepository = userRepository;
        // REMOVIDO: mapper.registerSubtypes(Consumer.class, Seller.class);
        // REMOVIDO: loadUsers(); // UserRepository já faz isso
    }

    // REMOVIDO: loadUsers()
    // REMOVIDO: saveUsers()
    // REMOVIDO: generateNextId()

    /**
     * Register a new user (assigns a unique ID and saves)
     * Returns Optional.empty() if email already exists.
     */
    public Optional<User> registerUser(User user) { 
        // Verificar se o email já existe usando o repositório
        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            System.out.println("DEBUG: UserService.registerUser - Registration failed: Email already exists.");
            return Optional.empty(); // Email já existe
        }

        // O ID será gerado e atribuído pelo UserRepository.save()
        userRepository.save(user); // Delega ao UserRepository
        System.out.println("DEBUG: UserService.registerUser - Registered new user: " + user.getEmail() + " with ID: " + user.getId());
        return Optional.of(user); // Retorna o usuário salvo (com ID atribuído pelo repo)
    }

    public List<User> getAllUsers() {
        return userRepository.getAll(); // Delega ao UserRepository
    }

    public User getUserById(int id) {
        System.out.println("DEBUG: UserService.getUserById - Searching for user with ID: " + id);
        User foundUser = userRepository.getById(id); // Delega ao UserRepository
        if (foundUser != null) {
            System.out.println("DEBUG: UserService.getUserById - Found user: " + foundUser.getEmail());
        } else {
            System.out.println("DEBUG: UserService.getUserById - User with ID " + id + " not found.");
        }
        return foundUser;
    }

    public boolean deleteUserById(int id) {
        System.out.println("DEBUG: UserService.deleteUserById - Attempting to delete user with ID: " + id);
        boolean removed = userRepository.deleteById(id); // Delega ao UserRepository
        if (removed) {
            System.out.println("DEBUG: UserService.deleteUserById - User with ID " + id + " removed successfully.");
        } else {
            System.out.println("DEBUG: UserService.deleteUserById - User with ID " + id + " not found for deletion.");
        }
        return removed;
    }

    public User findByEmail(String email) {
        System.out.println("DEBUG: UserService.findByEmail - Searching for email: '" + email + "'");
        Optional<User> user = userRepository.findUserByEmail(email); // Delega ao UserRepository
        if (user.isPresent()) {
            System.out.println("DEBUG: UserService.findByEmail - User found: " + user.get().getEmail() + " (ID: " + user.get().getId() + ")");
            return user.get();
        } else {
            System.out.println("DEBUG: UserService.findByEmail - User NOT found for email: '" + email + "'");
            return null;
        }
    }

    public boolean verifyPassword(String rawPassword, String storedPassword) {
        System.out.println("DEBUG: UserService.verifyPassword - Raw Password: '" + rawPassword + "'");
        System.out.println("DEBUG: UserService.verifyPassword - Stored Password: '" + storedPassword + "'");
        boolean matches = rawPassword.equals(storedPassword);
        System.out.println("DEBUG: UserService.verifyPassword - Passwords Match: " + matches);
        return matches;
    }

    public Optional<User> loginUser(String email, String password) {
        User user = findByEmail(email);
        if (user != null && verifyPassword(password, user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public Optional<User> updateUser(User userDetails) {
        User existingUser = getUserById(userDetails.getId());
        if (existingUser != null) {
            if (!existingUser.getEmail().equalsIgnoreCase(userDetails.getEmail())) {
                if (findByEmail(userDetails.getEmail()) != null) {
                    System.out.println("DEBUG: UserService - Update failed: Email already in use by another user.");
                    return Optional.empty();
                }
            }

            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setCpf(userDetails.getCpf());
            existingUser.setAddress(userDetails.getAddress());

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(userDetails.getPassword());
            }

            if (existingUser instanceof Consumer && userDetails instanceof Consumer) {
                Consumer existingConsumer = (Consumer) existingUser;
                Consumer updatedConsumerDetails = (Consumer) userDetails;
                existingConsumer.setPreferredPaymentMethod(updatedConsumerDetails.getPreferredPaymentMethod());
                System.out.println("DEBUG: UserService - Updated preferredPaymentMethod for Consumer ID " + existingConsumer.getId() + " to: " + existingConsumer.getPreferredPaymentMethod());
            }
            
            userRepository.save(existingUser); // Delega ao UserRepository para salvar a atualização
            return Optional.of(existingUser);
        }
        System.out.println("DEBUG: UserService - Update failed: User with ID " + userDetails.getId() + " not found.");
        return Optional.empty();
    }
}
