package ru.gazprombank.token.kms.service;

import ru.gazprombank.token.kms.entity.TokenType;

import java.time.LocalDateTime;
import java.util.Date;

public interface TokenService {
    String secret2Token(String secret, TokenType type, LocalDateTime expiryDate);
    String token2Secret(String token);
}
