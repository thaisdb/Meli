package com.meli.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * A helper/persistence class
 * Responsible for low level I/O, like loading/saving, to users.json with Jackson
 * and returning all users (or filtering by type/email/etc)
 */
public class UserRepository {
    private static final File USER_FILE = new File("data/users.json");
    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private List<User> users;

    public UserRepository() {
        users = loadUsers();
    }

    public static List<User> loadUsers() {
        try {
            if (!USER_FILE.exists()) return new ArrayList<>();
            return mapper.readValue(USER_FILE, new TypeReference<List<User>>() {});

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveUsers(List<User> users) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(USER_FILE, users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<User> getAll() {
        return users;
    }

    public User getById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    public void save(User user) {
        int maxId = users.stream().mapToInt(User::getId).max().orElse(0);
        user.setId(maxId + 1);
        users.add(user);
        saveUsers(users);
    }

    public boolean deleteById(int id) {
        boolean removed = users.removeIf(u -> u.getId() == id);
        if (removed) saveUsers(users);
        return removed;
    }
}
