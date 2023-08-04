package ru.gpb.kms.token.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
class TokenTypeConverter implements AttributeConverter<TokenType, String> {
    @Override
    public String convertToDatabaseColumn(TokenType type) {
        switch (type) {
            case PAN:
                return "PAN";
            default:
                return "PAN";
        }
    }

    @Override
    public TokenType convertToEntityAttribute(String s) {
        switch (s) {
            case "PAN":
                return TokenType.PAN;
            default:
                return TokenType.PAN;
        }
    }
}
