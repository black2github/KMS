package ru.gpb.token.entity.Dto;

import lombok.Builder;
import lombok.Data;
import ru.gpb.token.entity.TokenType;

/**
 * DTO class that contains secret data, type and time to live before expiration.
 */
@Data
@Builder
public class TokenResponse {
    private String secret;
    private TokenType type;
    private Long ttl; // Оставшееся время жизни в секундах
}
