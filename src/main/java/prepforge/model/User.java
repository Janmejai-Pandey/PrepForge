package prepforge.model;

/**
 * Abstract Base Class: User
 * 
 * Demonstrates:
 * 1. ABSTRACTION: Marked 'abstract' so it cannot be instantiated directly with 'new User()'.
 *    It serves as a clean common template for all human actors in PrepForge (Student, Admin).
 * 2. ENCAPSULATION: All fields are private, accessible only through public getters and setters.
 */
public abstract class User {

    // ---------------- Private Fields (Encapsulation) ----------------
    private int id;
    private String name;
    private String email;
    private String passwordHash;

    // ---------------- Constructors (Overloading) ----------------
    // Default constructor
    public User() {
    }

    // Constructor without ID (useful before saving to a database with auto-increment IDs)
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Parameterized constructor with ID
    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Parameterized constructor with password hash
    public User(int id, String name, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // ---------------- Abstract Methods (Polymorphism & Abstraction) ----------------
    // Every child class MUST provide its own implementation of these methods.
    public abstract String getRole();
    public abstract void showMenu();

    // ---------------- Getters and Setters (Encapsulation) ----------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Alias for getUserId / setUserId to support contracts expecting studentId/userId
    public int getUserId() {
        return id;
    }

    public void setUserId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", role=" + getRole() + "]";
    }
}
