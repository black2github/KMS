package ru.gazprombank.token.kms.entity;

/**
 * Key types.
 */
public enum KeyType {
    SYMMETRIC,
    // Secret key
    PRIVATE,
    // Public key or certificate
    PUBLIC
}
