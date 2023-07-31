package ru.gazprombank.token.kms.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class KeyTypeConverter implements AttributeConverter<KeyType, String> {
    @Override
    public String convertToDatabaseColumn(KeyType keyType) {
        switch (keyType) {
            case PUBLIC: return "PUBLIC";
            case PRIVATE: return "PRIVATE";
            default: return "SYMMETRIC";
        }
    }
    @Override
    public KeyType convertToEntityAttribute(String s) {
        switch (s) {
            case "PUBLIC": return KeyType.PUBLIC;
            case "PRIVATE": return KeyType.PRIVATE;
            default: return KeyType.SYMMETRIC;
        }
    }
}
