package com.sopan.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2 implementation using PBKDF2WithHmacSHA256, 600,000 iterations,
 * 16-byte random salt, and constant-time equality check.
 */
public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public HashResult hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);

        byte[] hashBytes = computeHash(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        String hashBase64 = Base64.getEncoder().encodeToString(hashBytes);

        // Store formatted string "PBKDF2:600000:<salt>:<hash>"
        String fullHash = String.format("PBKDF2:%d:%s:%s", ITERATIONS, saltBase64, hashBase64);

        return new HashResult(fullHash, saltBase64);
    }

    @Override
    public boolean verify(String password, String salt, String expectedHash) {
        if (password == null || salt == null || expectedHash == null) {
            return false;
        }

        int iterations = ITERATIONS;
        String actualSaltStr = salt;
        String expectedHashStr = expectedHash;

        if (expectedHash.startsWith("PBKDF2:")) {
            String[] parts = expectedHash.split(":");
            if (parts.length == 4) {
                try {
                    iterations = Integer.parseInt(parts[1]);
                    actualSaltStr = parts[2];
                    expectedHashStr = parts[3];
                } catch (NumberFormatException ignored) {}
            }
        }

        byte[] saltBytes;
        try {
            saltBytes = Base64.getDecoder().decode(actualSaltStr);
        } catch (IllegalArgumentException e) {
            return false;
        }

        byte[] computedBytes = computeHash(password.toCharArray(), saltBytes, iterations, KEY_LENGTH);
        String computedBase64 = Base64.getEncoder().encodeToString(computedBytes);

        // Constant-time compare using MessageDigest.isEqual
        return MessageDigest.isEqual(
                computedBase64.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                expectedHashStr.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    private byte[] computeHash(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 hashing failed", e);
        }
    }
}
