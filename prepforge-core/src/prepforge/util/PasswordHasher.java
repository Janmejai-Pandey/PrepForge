package prepforge.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import prepforge.exception.PrepForgeException;

/**
 * Utility Class: PasswordHasher
 * 
 * Implements industry-standard cryptographic password hashing using:
 * Algorithm: PBKDF2WithHmacSHA256
 * 
 * Demonstrates:
 * 1. SECURE PASSWORD STORAGE: Passwords are NEVER stored in plaintext.
 * 2. SALT: A random byte sequence added to each password before hashing to defeat
 *    Rainbow Tables and precomputed dictionary attacks.
 * 3. KEY STRETCHING (PBKDF2): Uses 10,000 iterations to make brute-force attacks computationally expensive.
 * 4. CONSTANT-TIME COMPARISON: MessageDigest.isEqual() prevents side-channel timing attacks.
 */
public class PasswordHasher {

    // Algorithm name requested by PrepForge specifications
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    // Number of hashing iterations (slows down brute-force attacks without impacting user experience)
    private static final int ITERATIONS = 10000;

    // Derived key length in bits
    private static final int KEY_LENGTH = 256;

    // Salt size in bytes (16 bytes = 128 bits of cryptographic entropy)
    private static final int SALT_BYTES = 16;

    // Private constructor prevents instantiation of this utility class
    private PasswordHasher() {
    }

    /**
     * Hashes a plain-text password using a newly generated cryptographic salt.
     * 
     * Output format: Base64(salt) + ":" + Base64(hash)
     * 
     * @param plainPassword Plain-text password entered by the user
     * @return Formatted hash string containing both salt and hash
     * @throws PrepForgeException If the cryptographic algorithm is unavailable
     */
    public static String hashPassword(String plainPassword) throws PrepForgeException {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new PrepForgeException("Cannot hash an empty password.");
        }

        // 1. Generate a cryptographically secure random salt
        byte[] salt = generateSalt();

        // 2. Hash the password with the salt using PBKDF2
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        // 3. Encode both to Base64 and combine with a colon separator
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        return saltBase64 + ":" + hashBase64;
    }

    /**
     * Verifies a plain-text password against a stored "salt:hash" string.
     * 
     * @param plainPassword The plain password entered during login
     * @param storedHash The stored string from the database in "salt:hash" format
     * @return true if password matches, false otherwise
     * @throws PrepForgeException If stored hash format is invalid or algorithm fails
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) throws PrepForgeException {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        // Split stored string into salt and hash parts
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            throw new PrepForgeException("Invalid stored password hash format.");
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        // Compute hash of the entered password using the original salt
        byte[] computedHash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        // Constant-time comparison prevents timing attacks
        return MessageDigest.isEqual(expectedHash, computedHash);
    }

    /**
     * Generates a random 16-byte cryptographic salt.
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Executes the PBKDF2 key derivation function.
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws PrepForgeException {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PrepForgeException("Cryptographic error while hashing password: " + e.getMessage(), e);
        }
    }
}
