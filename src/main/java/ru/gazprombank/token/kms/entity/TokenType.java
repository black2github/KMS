package ru.gazprombank.token.kms.entity;

/**
 * Key types.
 */
public enum TokenType {
    PAN("PAN");

    final String name;
    TokenType(String name) {
        this.name = name;
    }
}
