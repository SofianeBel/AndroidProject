package com.sofiane.newtwitter.repository;

import com.sofiane.newtwitter.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private static UserRepository instance;
    private Map<String, User> users;

    private UserRepository() {
        users = new HashMap<>();
        // Add some mock data
        addMockUsers();
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    private void addMockUsers() {
        User user1 = new User("1", "john_doe", "john@example.com");
        User user2 = new User("2", "jane_smith", "jane@example.com");
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
    }

    public User getUserById(String id) {
        return users.get(id);
    }

    public User getUserByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public void updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        }
    }

    public void deleteUser(String userId) {
        users.remove(userId);
    }
} 