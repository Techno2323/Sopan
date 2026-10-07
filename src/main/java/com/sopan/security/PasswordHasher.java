package com.sopan.security;

public interface PasswordHasher {

    record HashResult(String hash, String salt) {}

    HashResult hash(String password);

    boolean verify(String password, String salt, String hash);
}
