package com.sofiane.newtwitter.repository;

import com.sofiane.newtwitter.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository pour gérer les données des utilisateurs.
 * Cette classe implémente le pattern Singleton pour assurer une instance unique.
 * Elle fournit des méthodes pour accéder et manipuler les données des utilisateurs.
 */
public class UserRepository {
    private static UserRepository instance;
    private Map<String, User> users;

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * Initialise la map des utilisateurs.
     */
    private UserRepository() {
        users = new HashMap<>();
    }

    /**
     * Obtient l'instance unique du repository.
     * Crée une nouvelle instance si elle n'existe pas encore.
     *
     * @return L'instance unique de UserRepository
     */
    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id L'identifiant de l'utilisateur à récupérer
     * @return L'utilisateur correspondant à l'identifiant, ou null s'il n'existe pas
     */
    public User getUserById(String id) {
        return users.get(id);
    }

    /**
     * Récupère un utilisateur par son adresse email.
     *
     * @param email L'adresse email de l'utilisateur à récupérer
     * @return L'utilisateur correspondant à l'email, ou null s'il n'existe pas
     */
    public User getUserByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Récupère tous les utilisateurs.
     *
     * @return Une liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    /**
     * Ajoute un nouvel utilisateur.
     *
     * @param user L'utilisateur à ajouter
     */
    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    /**
     * Met à jour un utilisateur existant.
     *
     * @param user L'utilisateur à mettre à jour
     */
    public void updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        }
    }

    /**
     * Supprime un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur à supprimer
     */
    public void deleteUser(String userId) {
        users.remove(userId);
    }
} 