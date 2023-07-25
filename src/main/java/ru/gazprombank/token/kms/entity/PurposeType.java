package ru.gazprombank.token.kms.entity;

/**
 * Key purpose type.
 */
public enum PurposeType {
    // Data Encoding Key
    DEK,
    // Key Encoding Key
    KEK,
    // Cahnnel Encoding Key
    CEK,
    // Signature
    SIG
}
