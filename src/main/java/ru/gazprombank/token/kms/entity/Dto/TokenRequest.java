package ru.gazprombank.token.kms.entity.Dto;

import lombok.Builder;
import lombok.Data;
import ru.gazprombank.token.kms.entity.TokenType;

@Data
@Builder
public class TokenRequest {
    private String secret;
    private TokenType type;
    private Long ttl;
}
