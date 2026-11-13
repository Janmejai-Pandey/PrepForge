package com.prepforge.service;

import java.util.ArrayList;

import com.prepforge.exception.InvalidLoginException;
import com.prepforge.exception.RegistrationException;
import com.prepforge.model.User;
import com.prepforge.util.PasswordUtil;

// SERVICE CLASS: AuthService
// Handles user registration and login.
public class AuthService {

    // A simple ArrayList to store our registered users (acts like an in-memory database)
    private ArrayList<User> registeredUsers = new ArrayList<>();

    // -------------------------------------------------------------
    // 1. REGISTRATION METHOD
    // -------------------------------------------------------------
    public void registerUser(User user, String plainPassword) throws RegistrationException {
        // Validation: user cannot be null
        if (user == null) {
            throw new RegistrationException("Registration failed: User details cannot be empty!");
        }

        // Validation: password length rule (must be at least 6 characters)
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new RegistrationException("Registration failed: Password must be at least 6 characters long!");
        }

        // Validation: check if email is already used by another user
        for (User u : registeredUsers) {
            if (u.getEmail().equalsIgnoreCase(user.getEmail())) {
                throw new RegistrationException("Registration failed: Email '" + user.getEmail() + "' is already registered!");
            }
        }

        // Security: Hash the password before saving
        String hash = PasswordUtil.hashPassword(plainPassword);
        user.setPasswordHash(hash);

        // Save user into our list
        registeredUsers.add(user);
        System.out.println("[Registration Success] Registered " + user.getUserRole() + ": " + user.getName());
    }

    // -------------------------------------------------------------
    // 2. LOGIN METHOD (POLYMORPHISM IN ACTION!)
    // -------------------------------------------------------------
    // Look at the return type: it returns "User".
    // Even though the actual person logging in is a Student or Admin,
    // we return the parent type User! This is Polymorphism.
    public User login(String email, String plainPassword) throws InvalidLoginException {
        // Validation: inputs cannot be empty
        if (email == null || plainPassword == null) {
            throw new InvalidLoginException("Email and password are required!");
        }

        // Search for user in our list
        for (User u : registeredUsers) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                
                // User found! Now check if the password hash matches
                boolean isPasswordCorrect = PasswordUtil.checkPassword(plainPassword, u.getPasswordHash());
                
                if (isPasswordCorrect) {
                    System.out.println("[Login Success] Welcome, " + u.getName() + " (" + u.getUserRole() + ")");
                    return u; // Returns the polymorphic User reference
                } else {
                    // Password does not match
                    throw new InvalidLoginException("Login failed: Incorrect password for " + email);
                }
            }
        }

        // If loop finished and no user matched the email
        throw new InvalidLoginException("Login failed: No account registered with email '" + email + "'");
    }

    // Helper method to check how many users are registered
    public int getTotalUsers() {
        return registeredUsers.size();
    }
}
