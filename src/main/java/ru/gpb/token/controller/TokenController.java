package ru.gpb.token.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gpb.token.entity.Dto.TokenRequest;
import ru.gpb.token.entity.Dto.TokenResponse;
import ru.gpb.token.service.TokenService;

/**
 * Работа с токенами.
 */
@RestController
@RequestMapping("/tokens/")
@RequiredArgsConstructor
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

    private final TokenService tokenService;

    /**
     * Преобразование секретных данных в токен.
     * @param request TokenRequest с данными.
     * @return строка токена.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public  ResponseEntity<String> secret2Token(@RequestBody TokenRequest request) {
        log.info("secret2Token: <- type=" + request.getType());

        String token = tokenService.secret2Token(request);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/get")
    @PreAuthorize("hasRole('KMS')")
    public  ResponseEntity<TokenResponse> token2Secret(@RequestBody String token) {
        log.info("token2Secret: <- token=" + token);

        TokenResponse secret = tokenService.token2Secret(token);
        return new ResponseEntity<>(secret, HttpStatus.OK);
    }
}
