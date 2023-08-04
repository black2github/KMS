package ru.gpb.kms.token.entity;

/**
 * Key types.
 */
public enum KeyType {
    SYMMETRIC("SYMMETRIC"),
    // Secret key
    PRIVATE("PRIVATE"),
    // Public key or certificate
    PUBLIC("PUBLIC");

    final String name;
    KeyType(String name) {
        this.name = name;
    }
}
