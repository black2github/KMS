package ru.gpb.kms.token.entity.Dto;

import lombok.Builder;
import lombok.Data;
import ru.gpb.kms.token.entity.TokenType;

@Data
@Builder
public class TokenRequest {
    private String secret;
    private TokenType type;
    private Long ttl;
}
