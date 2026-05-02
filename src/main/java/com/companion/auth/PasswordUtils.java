package com.companion.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class PasswordUtils {

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Decode the Base64 salt back to bytes, use as digest prefix
            md.update(Base64.getDecoder().decode(salt));
            // Always use UTF-8 to avoid platform-default charset mismatches
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        if (inputPassword == null || storedHash == null || storedSalt == null) return false;
        String computed = hashPassword(inputPassword, storedSalt);
        return computed.equals(storedHash);
    }
}
