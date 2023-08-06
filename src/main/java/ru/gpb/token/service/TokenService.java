package ru.gpb.token.service;

import ru.gpb.token.entity.Dto.TokenRequest;
import ru.gpb.token.entity.Dto.TokenResponse;

public interface TokenService {
    String secret2Token(TokenRequest request);

    TokenResponse token2Secret(String token);
}
