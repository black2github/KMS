package ru.gazprombank.token.kms.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final KeyDataRepository keyDataRepository;
    private final KeyDataService keyDataService;

    /**
     * Преобразовать секретные данные в токен.
     *
     * @param secret
     * @param type
     * @param expiryDate
     * @return
     */
    @Override
    @Transactional
    public String secret2Token(String secret, TokenType type, LocalDateTime expiryDate) {
        log.info("secret2Token: <- type=" + type);

        // Создание токена
        Token token = new Token().builder()
                .createdDate(LocalDateTime.now())
                .type(type)
                .build();
        if (type == TokenType.PAN) {
            token.setIndex(maskPan(secret));
            if (expiryDate != null) {
                token.setExpiryDate(expiryDate);
            } else {
                token.setExpiryDate(LocalDateTime.now().plusYears(1));
            }
        }

        // Получить ключ шифрования данных
        List<KeyData> keys = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.ENABLED);
        if (keys.isEmpty())
            throw new KeyNotFoundApplicationException("Не найден подходящий ключ шифрования данных");

        // Берем первый доступный ключ
        KeyData key = keys.get(0);
        try {
            SecretKey dataKey = keyDataService.decodeDataKey(key.getId());
            token.setSecret(KeyGenerator.encryptData(secret, dataKey));
        } catch (Exception ex) {
            log.error("secret2Token: Ошибка создания токена:" + ex.getMessage());
            throw new SecurityApplicationException("Ошибка создания токена:" + ex.getMessage());
        }
        // Сохранение ссылки на ключ шифрования
        token.setKey(key);
        tokenRepository.saveAndFlush(token);

        log.info("secret2Token: -> " + token.getId());
        // возврат id в качестве токена
        return token.getId().toString();
    }

    private String maskPan(String pan) {
        return "******" + pan.substring(6, pan.length() - 4) + "****";
    }

    /**
     * Получить по токену секретные данные.
     *
     * @param id
     * @return
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
                () -> new KeyNotFoundApplicationException("Ключ шифрования токена " + data.getKey().getId() + " не найден"));
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
}
