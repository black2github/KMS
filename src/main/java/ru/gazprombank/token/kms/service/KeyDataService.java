package ru.gazprombank.token.kms.service;

import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface KeyDataService {

    List<KeyDataDto> listAll();

    void updateKeyData(String id, KeyDataDto keyDataDto);
    void changeStatus(String id, KeyStatus newStatus);

    KeyData saveKeyData(KeyDataDto keyDataDto);

    void delete(String uid);

    /**
     * Получение секретного ключа шифрования данных в открытом виде.
     * @param id
     * @return
     */
    SecretKey decodeDataKey(UUID id);

    /**
     * Создание ключа шифрования данных.
     * @param alias
     * @return
     */
    KeyDataDto generateDataKey(String alias);

    /**
     * Загрузка мастер-ключа.
     * @param id
     * @param password
     * @return
     */
    KeyDataDto loadMasterKey(UUID id, char[] password);

    /**
     * Создание мастер-ключа.
     * @param id Идентификатор (необязательный)
     * @param alias Алиас (необязательный)
     * @param desc Описание (необязательный)
     * @param expirationDate Срок действия (необязательный)
     * @param password Пароль
     * @param notifyDate Срок уведомления об истечении срока действия (необязательный)
     * @return
     */
    KeyDataDto generateMasterKey(UUID id, String alias, String desc, LocalDateTime expirationDate,
                            char[] password, LocalDateTime notifyDate);
}
