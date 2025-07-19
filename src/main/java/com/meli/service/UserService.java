package com.meli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.meli.model.User;
import com.meli.model.Consumer; 
import com.meli.model.Seller;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Importar Optional

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
        // Configurar ObjectMapper para lidar com polimorfismo
        mapper.registerSubtypes(Consumer.class, Seller.class);
        loadUsers();
    }

    private void loadUsers() {
        System.out.println("DEBUG: UserService.loadUsers - Trying to load users from: " + new File(DATA_FILE_PATH).getAbsolutePath());
        try {
            File file = new File(DATA_FILE_PATH);
            if (file.exists() && file.length() > 0) { // Verifica se o arquivo existe e não está vazio
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
                users = mapper.readValue(file, listType);
                System.out.println("DEBUG: UserService.loadUsers - Successfully loaded " + users.size() + " users.");
            } else {
                System.out.println("DEBUG: UserService.loadUsers - No user file found or file is empty. Starting with empty list.");
                users = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("ERROR: UserService.loadUsers - Failed to load users from file: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed error info
            users = new ArrayList<>(); // Garante que a lista não seja nula em caso de erro
        }
    }

    private void saveUsers() {
        System.out.println("DEBUG: UserService.saveUsers - Attempting to save " + users.size() + " users to file.");
        try {
            File file = new File(DATA_FILE_PATH);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // Cria o diretório 'data' se não existir
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
            System.out.println("DEBUG: UserService.saveUsers - Users saved successfully.");
        } catch (IOException e) {
            System.err.println("ERROR: UserService.saveUsers - Failed to save users to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register a new user (assigns a unique ID and saves)
     * Returns Optional.empty() if email already exists.
     */
    public Optional<User> registerUser(User user) { 
        // Verificar se o email já existe
        if (findByEmail(user.getEmail()) != null) { // Usando o método findByEmail existente
            System.out.println("DEBUG: UserService.registerUser - Registration failed: Email already exists.");
            return Optional.empty(); // Email já existe
        }

        // Você pode adicionar validação para CPF único aqui se for um requisito
        // if (findByCpf(user.getCpf()) != null) { ... }

        int newId = generateNextId();
        user.setId(newId);
        users.add(user);
        saveUsers();
        System.out.println("DEBUG: UserService.registerUser - Registered new user: " + user.getEmail() + " with ID: " + user.getId());
        return Optional.of(user); // Retorna o usuário salvo
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users); // Retorna uma cópia para evitar modificações externas
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
     * Find user by given email, used in login page and for uniqueness checks
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

    // REMOVIDO: findByUsername

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

    // NOVO MÉTODO: loginUser para ser usado pelo Controller
    public Optional<User> loginUser(String email, String password) {
        User user = findByEmail(email); // Usar findByEmail para login
        if (user != null && verifyPassword(password, user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // NOVO MÉTODO: updateUser para ser usado pelo Controller
    public Optional<User> updateUser(User userDetails) {
        User existingUser = getUserById(userDetails.getId());
        if (existingUser != null) {
            // Verificar se o novo email já existe e não pertence ao usuário atual
            if (!existingUser.getEmail().equalsIgnoreCase(userDetails.getEmail())) {
                if (findByEmail(userDetails.getEmail()) != null) {
                    System.out.println("DEBUG: UserService - Update failed: Email already in use by another user.");
                    return Optional.empty(); // Email já em uso por outro usuário
                }
            }

            // REMOVIDO: Lógica de verificação de username

            // Atualizar os campos permitidos
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setCpf(userDetails.getCpf());
            existingUser.setAddress(userDetails.getAddress()); // Atualizar endereço

            // A senha só deve ser atualizada se for fornecida (e não vazia)
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(userDetails.getPassword());
            }

            if (existingUser instanceof Consumer && userDetails instanceof Consumer) {
                Consumer existingConsumer = (Consumer) existingUser;
                Consumer updatedConsumerDetails = (Consumer) userDetails;
                existingConsumer.setPreferredPaymentMethod(updatedConsumerDetails.getPreferredPaymentMethod());
                System.out.println("DEBUG: UserService - Updated preferredPaymentMethod for Consumer ID " + existingConsumer.getId() + " to: " + existingConsumer.getPreferredPaymentMethod());
            }
        
            saveUsers(); // Salva as alterações
            return Optional.of(existingUser);
        }
        System.out.println("DEBUG: UserService - Update failed: User with ID " + userDetails.getId() + " not found.");
        return Optional.empty();
    }
}
