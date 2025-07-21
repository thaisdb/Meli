package com.meli.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private static final File USER_FILE = new File("data/users.json");
    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules(); 
    private List<User> users;

    public UserRepository() {
        users = loadUsers();
    }

    public static List<User> loadUsers() {
        try {
            if (!USER_FILE.exists() || USER_FILE.length() == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(USER_FILE, new TypeReference<List<User>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> usersToSave) {
        try {
            File parentDir = USER_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(USER_FILE, usersToSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<User> getAll() {
        return new ArrayList<>(users); 
    }

    public User getById(int id) {
        // Não precisa de null check para id, pois é int primitivo
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public Optional<User> findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Salva um usuário. Se o usuário já possui um ID (diferente de 0), ele é atualizado.
     * Caso contrário (ID é 0), um novo ID é gerado e o usuário é adicionado.
     * @param user O usuário a ser salvo.
     */
    public void save(User user) {
        List<User> currentUsers = loadUsers();

        boolean found = false;
        // Se o ID do usuário é diferente de 0, tenta encontrar e atualizar
        if (user.getId() != 0) { // CORREÇÃO AQUI: Usa 0 para indicar novo, não null
            for (int i = 0; i < currentUsers.size(); i++) {
                if (currentUsers.get(i).getId() == user.getId()) { // CORREÇÃO AQUI: Comparação direta de int
                    currentUsers.set(i, user);
                    found = true;
                    System.out.println("DEBUG: UserRepository.save - Updated existing user with ID: " + user.getId());
                    break;
                }
            }
        }

        if (!found) {
            // Se o ID era 0 ou não foi encontrado, gera um novo ID e adiciona
            int newId = currentUsers.stream()
                                    .mapToInt(User::getId)
                                    .max()
                                    .orElse(0) + 1;
            user.setId(newId);
            currentUsers.add(user);
            System.out.println("DEBUG: UserRepository.save - Added new user with generated ID: " + newId);
        }
        saveUsers(currentUsers);
        this.users = currentUsers;
    }

    public boolean deleteById(int id) {
        List<User> currentUsers = loadUsers();
        boolean removed = currentUsers.removeIf(u -> u.getId() == id); // Não precisa de null check
        if (removed) {
            saveUsers(currentUsers);
            this.users = currentUsers;
        }
        return removed;
    }
}
