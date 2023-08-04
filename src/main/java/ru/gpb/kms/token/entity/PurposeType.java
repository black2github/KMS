package ru.gpb.kms.token.entity;

/**
 * Key purpose type.
 */
public enum PurposeType {
    // Data Encoding Key
    DEK("DEK"),
    // Key Encoding Key
    KEK("KEK"),
    // Cahnnel Encoding Key
    CEK("CEK"),
    // Signature
    SIG("SIG");

    private final String name;
    PurposeType(String name) {
        this.name = name;
    }
}
