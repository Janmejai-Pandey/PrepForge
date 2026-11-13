package com.prepforge.util;

// HELPER CLASS: PasswordUtil
//
// In real software, we NEVER store plain-text passwords in a database!
// Instead, we convert the password into a scrambled string called a "hash".
//
// Here, we use Java's built-in hashCode() to easily turn passwords into
// scrambled numbers so anyone reading your code can understand it immediately.
public class PasswordUtil {

    // Converts a plain password into a scrambled hash string
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return "";
        }
        // Math.abs(plainPassword.hashCode()) turns the password into a positive number
        return "HASH_" + Math.abs(plainPassword.hashCode());
    }

    // Checks if the typed password matches the saved hash
    public static boolean checkPassword(String plainPassword, String storedHash) {
        String generatedHash = hashPassword(plainPassword);
        return generatedHash.equals(storedHash);
    }
}
