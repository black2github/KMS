package ru.gazprombank.token.kms.service;

import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface KeyDataService {

    List<KeyData> listAll();

    void updateKeyData(KeyDataDto keyDataDto);

    KeyData saveKeyData(KeyDataDto keyDataDto);

    void delete(KeyDataDto keyDataDto);

    /**
     * Создание ключа шифрования данных.
     * @param alias
     * @return
     */
    KeyData createDataKey(String alias);

    /**
     * Загрузка мастер-ключа.
     * @param id
     * @param password
     * @return
     */
    KeyData loadMasterKey(UUID id, char[] password);

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
    KeyData generateMasterKey(UUID id, String alias, String desc, LocalDateTime expirationDate,
                            char[] password, LocalDateTime notifyDate);
}
