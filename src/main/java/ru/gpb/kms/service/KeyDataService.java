package ru.gpb.kms.service;

import ru.gpb.kms.entity.Dto.KeyDataDto;
import ru.gpb.kms.entity.KeyStatus;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Key Data interface.
 */
public interface KeyDataService {

    /**
     * Список всех ключей.
     * @return
     */
    List<KeyDataDto> listAll();

    /**
     * Список идентификаторов ключей в оепративном доступе.
     * @return
     */
    List<String> listCache();

    /**
     * Обновление атрибутов ключа.
     * @param id строка с идентификатором ключа.
     * @param keyDataDto
     */
    void updateKeyData(String id, KeyDataDto keyDataDto);

    /**
     * Изменение статуса ключа.
     * @param id строка с идентификатором ключа.
     * @param newStatus
     */
    void changeStatus(String id, KeyStatus newStatus);

    /**
     * Отметить ключ на удаление.
     * @param uid
     */
    void delete(String uid);

    /**
     * Получение секретного ключа шифрования данных в открытом виде.
     * @param id
     * @return
     */
    SecretKey decodeDataKey(UUID id);

    /**
     * Создание ключа шифрования данных.
     * @param alias адиас ключа, с которым он будет помещен в хранилище.
     * @return
     */
    KeyDataDto generateDataKey(String alias);

    /**
     * Загрузка мастер-ключа. Для загрузки требуется вызвать данный метод двумя разными пользователями,
     * каждый из которых предоставляет свой пароль для доступа к ключу. Порядок вызова метода каждым из
     * пользователей должен совпадать порядок, использованным для генерации этого ключа.
     *
     * @param id идентификатор загружаемого ключа.
     * @param password
     * @return
     */
    KeyDataDto loadMasterKey(UUID id, char[] password);

    /**
     * Создание мастер-ключа. Для загрузки требуется вызвать данный метод двумя разными пользователями,
     * каждый из которых предоставляет свой пароль для доступа к ключу. Важно запонить не только пароли
     * пользователей, но и порядок вызова (какой пользователь первым, какой - вторым). При загрузке ключа
     * потребуется возпроизвести этот же порядок вызова.
     *
     * @param id Идентификатор ключа. Пустой в случае вызова первым пользователем и содержащий идентификатор
     *           создаваемого ключа (в статусе "ожидает создания") при вызове вторым пользователем.
     * @param alias Алиас (необязательный), под которым ключ будет помещен в храналище.
     * @param desc Описание (необязательный) ключа.
     * @param expirationDate Срок действия (необязательный). Если не указан, будет взят из настройки.
     * @param password Пароль для доступа к ключу.
     * @param notifyDate Срок уведомления об истечении срока действия (необязательный).
     * @return
     */
    KeyDataDto generateMasterKey(UUID id, String alias, String desc, LocalDateTime expirationDate,
                            char[] password, LocalDateTime notifyDate);


    /**
     * Перешифровка ключей шифрования данных.
     */
    void rotateDataKey();
}
