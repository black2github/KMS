package ru.gazprombank.token.kms.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.entity.Token;
import ru.gazprombank.token.kms.entity.TokenType;
import ru.gazprombank.token.kms.repository.KeyDataRepository;
import ru.gazprombank.token.kms.repository.TokenRepository;
import ru.gazprombank.token.kms.service.KeyDataService;
import ru.gazprombank.token.kms.service.TokenService;
import ru.gazprombank.token.kms.util.KeyGenerator;
import ru.gazprombank.token.kms.util.exceptions.InvalidArgumentApplicationException;
import ru.gazprombank.token.kms.util.exceptions.KeyNotFoundApplicationException;
import ru.gazprombank.token.kms.util.exceptions.SecurityApplicationException;
import ru.gazprombank.token.kms.util.exceptions.TokenNotFoundApplicationException;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final KeyDataRepository keyDataRepository;
    private final KeyDataService keyDataService;

    @Autowired
    private Environment env;

    /**
     * Преобразовать секретные данные в токен.
     *
     * @param secret строка с оригинальными секретными данными.
     * @param type   тип токена, по умолчанию PAN.
     * @param ttl    число секунд до протухания токена. В случае отсутствия - используется 1 год от текущей даты.
     * @return строка с идентификатором токена.
     */
    @Override
    @Transactional
    public String secret2Token(String secret, TokenType type, Long ttl) {
        log.info("secret2Token: <- type=" + type);

        // Поиск токена среди уже существующих
        String tokenId = findAlike(secret, type);
        if (tokenId != null) {
            // TODO игнорируется ttl, корректно ли это?
            return tokenId;
        }

        // Создание токена
        Token token = new Token().builder()
                .createdDate(LocalDateTime.now())
                .type(type)
                .build();
        if (type == TokenType.PAN) {
            if (secret == null || !secret.matches("[0-9]{16,19}"))
                throw new InvalidArgumentApplicationException("PAN должен иметь от 16 до 19 цифр");
            token.setIndex(maskPan(secret));
            if (ttl != null) {
                token.setExpiryDate(LocalDateTime.now().plusSeconds(ttl));
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

        // Получить ключ шифрования данных
        List<KeyData> keys = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.ENABLED);
        if (keys.isEmpty())
            throw new KeyNotFoundApplicationException("Не найден подходящий ключ шифрования данных");

        // Перебираем ключи шифрования данных
        KeyData key = null;
        for (KeyData k : keys) {
            try {
                SecretKey dataKey = keyDataService.decodeDataKey(k.getId());
                token.setSecret(KeyGenerator.encryptData(secret, dataKey));
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
        tokenRepository.saveAndFlush(token);

        log.info("secret2Token: -> " + token.getId());
        // возврат id в качестве токена
        return token.getId().toString();
    }

    private String maskPan(String pan) {
        return pan.substring(0, 6)
                + pan.substring(6, pan.length() - 4).replaceAll("[0-9]", "*")
                + pan.substring(pan.length() - 4);
    }

    /**
     * Получить по токену секретные данные.
     *
     * @param id строковое представляние токена.
     * @return строка с оригинальными секретными данными.
     */
    @Override
    public String token2Secret(String id) {
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
                () -> new TokenNotFoundApplicationException("Токен с идентификатором '" + id + "' не найден."));
        // Взять ключ шифрования данных, которым зашифрован токен
        KeyData keyData = keyDataRepository.findById(data.getKey().getId()).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ шифрования '" + data.getKey().getId() + "' токена не найден"));
        // Получить секретный ключ
        SecretKey secretKey = keyDataService.decodeDataKey(keyData.getId());
        // Расшифровать токен
        try {
            return KeyGenerator.decryptData(data.getSecret(), secretKey);
        } catch (Exception ex) {
            log.error("token2Secret: Ошибка расшифровки токена: " + ex.getMessage());
            throw new SecurityApplicationException("Ошибка расшифровки токена");
        }
    }

    /*
     * Поиск среди похожих токенов по индексу.
     * В случае нахождения - возврат идентификатора токена.
     */
    private String findAlike(String sec, TokenType type) {
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
                        return token.getId().toString();
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
}
