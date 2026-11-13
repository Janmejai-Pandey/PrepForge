package com.prepforge.model;

// ABSTRACT BASE CLASS: User
//
// 1. ABSTRACTION:
//    - Marked "abstract", meaning we cannot write "new User()".
//    - It acts as a template for Student and Admin.
//
// 2. ENCAPSULATION:
//    - All variables are "private" to hide the internal data.
//    - Access is allowed only through public getters and setters with validation.
public abstract class User {

    // ---------------- Private Fields (Encapsulation) ----------------
    private String userId;
    private String name;
    private String email;
    private String passwordHash;

    // Constructor to initialize basic user details
    public User(String userId, String name, String email) {
        setUserId(userId);
        setName(name);
        setEmail(email);
        this.passwordHash = ""; // Initially empty before registration hashes it
    }

    // ---------------- Abstract Methods (Abstraction & Polymorphism) ----------------
    // Every child class (Student, Admin) MUST write its own version of these methods!
    public abstract String getUserRole();
    public abstract void displayDetails();

    // ---------------- Validated Getters & Setters (Encapsulation) ----------------
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty!");
        }
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty!");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Simple, beginner-friendly validation: an email must contain "@" and "."
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email! Email must contain '@' and '.'");
        }
        this.email = email.toLowerCase().trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty!");
        }
        this.passwordHash = passwordHash;
    }
}
