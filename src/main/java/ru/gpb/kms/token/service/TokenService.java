package ru.gpb.kms.token.service;

import ru.gpb.kms.token.entity.TokenType;

public interface TokenService {
    String secret2Token(String secret, TokenType type, Long timeToLife);

    String token2Secret(String token);
}
