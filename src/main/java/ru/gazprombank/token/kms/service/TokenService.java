package ru.gazprombank.token.kms.service;

import ru.gazprombank.token.kms.entity.TokenType;

public interface TokenService {
    String secret2Token(String secret, TokenType type, Long timeToLife);
    String token2Secret(String token);
}
