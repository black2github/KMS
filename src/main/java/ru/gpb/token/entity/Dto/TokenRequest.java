package ru.gpb.token.entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.gpb.token.entity.TokenType;

/**
 * Used for secret data tokenization.
 */
@Data
@Builder
@AllArgsConstructor
public class TokenRequest {
    private String secret;
    private TokenType type;
    private Long ttl;
    // Параметры для связи токенизации с внешними процессами/системами
    private String systemID;
    private String channel;
    private String requestID;
}
