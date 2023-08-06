package ru.gpb.token.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.gpb.kms.entity.KeyData;
import ru.gpb.kms.entity.KeyStatus;
import ru.gpb.kms.entity.KeyType;
import ru.gpb.kms.entity.PurposeType;
import ru.gpb.kms.repository.KeyDataRepository;
import ru.gpb.kms.service.KeyDataService;
import ru.gpb.kms.util.KeyGenerator;
import ru.gpb.kms.util.exceptions.InvalidArgumentApplicationException;
import ru.gpb.kms.util.exceptions.KeyNotFoundApplicationException;
import ru.gpb.kms.util.exceptions.SecurityApplicationException;
import ru.gpb.token.entity.Dto.TokenRequest;
import ru.gpb.token.entity.Dto.TokenResponse;
import ru.gpb.token.entity.Token;
import ru.gpb.token.entity.TokenHistory;
import ru.gpb.token.entity.TokenType;
import ru.gpb.token.repository.TokenHistoryRepository;
import ru.gpb.token.repository.TokenRepository;
import ru.gpb.token.service.TokenService;
import ru.gpb.token.util.exeptions.TokenNotFoundApplicationException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Token service implementation class.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since 31.07.2023
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final KeyDataRepository keyDataRepository;
    private final KeyDataService keyDataService;

    @Autowired
    private Environment env;

    /**
     * Преобразовать секретные данные в токен.
     *
     * @param request TokenRequest с данными для создания токена.
     * @return строка с токеном.
     */
    @Override
    public String secret2Token(TokenRequest request) {
        log.info("secret2Token: <- type=" + request.getType());

        // Поиск токена среди уже существующих
        Token token = findAlike(request.getSecret(), request.getType());
        if (token != null) {
            // сохранение в истории токена
            tokenHistoryRepository.save(new TokenHistory(
                    null, token, LocalDateTime.now(), "secret2Token", getUserInfo(), request.getRequestID(), request.getChannel(), request.getSystemID()));

            // TODO игнорируется ttl, корректно ли это?
            return token.getId().toString();
        }

        // Создание токена
        token = new Token().builder()
                .createdDate(LocalDateTime.now())
                .type(request.getType())
                .build();
        if (request.getType() == TokenType.PAN) {
            if (request.getSecret() == null || !request.getSecret().matches("[0-9]{16,19}"))
                throw new InvalidArgumentApplicationException("PAN должен иметь от 16 до 19 цифр");
            token.setIndex(maskPan(request.getSecret()));
            if (request.getTtl() != null) {
                token.setExpiryDate(LocalDateTime.now().plusSeconds(request.getTtl()));
            } else {
                // взять время из настройки.
                try {
                    String sec = env.getProperty("kms.token.ttl.PAN");
                    // log.info(String.format("kms.token.ttl.PAN = %d, now=%s", Long.parseLong(sec), LocalDateTime.now()));
                    token.setExpiryDate(LocalDateTime.now().plusSeconds(Long.parseLong(sec)));
                    // log.info(String.format("новая дата %s", token.getExpiryDate()));
                } catch (Exception ex) {
                    log.warn(String.format("secret2Token: Неверный формат настройки kms.token.ttl.PAN (%s). Установлено по умолчанию - 1 год.", ex.getMessage()));
                    token.setExpiryDate(LocalDateTime.now().plusYears(1));
                }
            }
        } else {
            throw new InvalidArgumentApplicationException("Типы токенов, отличные от PAN, пока не поддерживаются");
        }

        // Получить список приемлемых ключей шифрования данных
        List<KeyData> keys = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.ENABLED);
        if (keys.isEmpty())
            throw new KeyNotFoundApplicationException("Не найден подходящий ключ шифрования данных");

        // Перебираем ключи шифрования данных
        KeyData key = null;
        for (KeyData k : keys) {
            try {
                // TODO подумать, чтобы заменить на метод без цикла, возвращающий нужынй ключ
                SecretKey dataKey = keyDataService.decodeDataKey(k.getId());
                token.setSecret(KeyGenerator.encryptData(request.getSecret(), dataKey));
                key = k;
            } catch (Exception ex) {
                log.warn("secret2Token: Ошибка создания токена:" + ex.getMessage());
                // переходим к следующему ключу
            }
        }
        if (key == null) {
            throw new SecurityApplicationException("Ошибка создания токена: Не найден ни один подходящий ключ шифрования данных");
        }

        // Сохранение ссылки на ключ шифрования
        token.setKey(key);
        tokenRepository.save(token);

        // сохранение в истории токена
        tokenHistoryRepository.save(new TokenHistory(
                null, token, LocalDateTime.now(), "secret2Token", getUserInfo(), request.getRequestID(), request.getChannel(), request.getSystemID()));

        log.info("secret2Token: -> " + token.getId());

        // возврат id в качестве токена
        return token.getId().toString();
    }

    /*
    * Маскировка PANa
     */
    private String maskPan(String pan) {
        return pan.substring(0, 6)
                + pan.substring(6, pan.length() - 4).replaceAll("[0-9]", "*")
                + pan.substring(pan.length() - 4);
    }

    /**
     * Получить по токену секретные данные, если ранее получал токен.
     *
     * @param id id строковое представление токена.
     * @return TokenResponse данные.
     */
    @Override
    public TokenResponse token2Secret(String id) {
        UUID uuid;
        log.info("token2Secret: <- token=" + id);

        try {
            uuid = UUID.fromString(id);
        } catch (Exception ex) {
            String msg = "Неверный формат токена";
            log.error("token2Secret: " + msg + ":" + ex.getMessage());
            throw new InvalidArgumentApplicationException(msg);
        }

        // Найти токен
        Token data = tokenRepository.findById(uuid).orElseThrow(
                () -> new TokenNotFoundApplicationException("Токен '" + id + "' не найден"));
        // Токен еще не протух?
        if (data.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new SecurityApplicationException("Срок действия токена '" + id + "' истек");
        }
        // Проверить, что токен выдавался ранее именно данному пользователю
        // TODO данная проверка нужна только непривелигированному пользователю - вынести в отдельный метод
        List<TokenHistory> history = tokenHistoryRepository.findByTokenAndMethodAndUser(data, "secret2Token", getUserInfo());
        if (history.isEmpty()) {
            throw new SecurityApplicationException("Нет прав на получение данных токена");
        }

        // Взять ключ шифрования данных, которым зашифрован токен
        KeyData keyData = keyDataRepository.findById(data.getKey().getId()).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ шифрования '" + data.getKey().getId() + "' токена не найден"));
        // Получить секретный ключ
        SecretKey secretKey = keyDataService.decodeDataKey(keyData.getId());
        // Расшифровать токен
        try {
            // сохранение в истории токена
            tokenHistoryRepository.save(new TokenHistory(
                    null, data, LocalDateTime.now(), "token2Secret", getUserInfo(), "doc1", null, "Eco"));

            return TokenResponse.builder()
                    .secret(KeyGenerator.decryptData(data.getSecret(), secretKey))
                    .ttl(Duration.between(data.getExpiryDate(), LocalDateTime.now()).getSeconds())
                    .build();

        } catch (Exception ex) {
            log.error("token2Secret: Ошибка расшифровки токена: " + ex.getMessage());
            throw new SecurityApplicationException("Ошибка расшифровки токена");
        }
    }

    /*
     * Поиск среди похожих токенов по индексу и расшифровкой содержимого.
     * В случае нахождения - возврат идентификатора токена.
     */
    private Token findAlike(String sec, TokenType type) {
        log.info("findAlike: <- ");

        if (type == TokenType.PAN) {
            // поиск по маске
            String mask = maskPan(sec);
            List<Token> tokens = tokenRepository.findByIndex(mask);
            if (tokens.isEmpty()) return null;
            // найдены потенциально похожие записи
            for (Token token : tokens) {
                try {
                    // расшифровываем каждый токен для сравнения
                    SecretKey secretKey = keyDataService.decodeDataKey(token.getKey().getId());

                    String sec2 = KeyGenerator.decryptData(token.getSecret(), secretKey);
                    if (sec.equals(sec2)) {
                        // возвращаем идентификатор существующего токена
                        return token;
                    }
                } catch (Exception ex) {
                    log.error("findAlike: Ошибка расшифровки токена " + token.getId());
                    // ignore
                }
            }
        }
        // совпадений не найдено
        return null;
    }

    /*
     * Метод для отладки логики работы разными пользователями.
     */
    private String getUserInfo() {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

        if (currentUser != null && currentUser.isAuthenticated()) {
            Object principal = currentUser.getPrincipal();

            if (principal instanceof UserDetails) {
                // return (UserDetails) principal;
                return currentUser.getName();
            }
        }
        return null;
    }
}
