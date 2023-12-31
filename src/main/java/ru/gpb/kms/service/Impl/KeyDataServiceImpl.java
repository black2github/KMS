package ru.gpb.kms.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gpb.kms.entity.KeyData;
import ru.gpb.kms.entity.Dto.KeyDataDto;
import ru.gpb.kms.entity.KeyDataHistory;
import ru.gpb.kms.entity.KeyStatus;
import ru.gpb.kms.entity.KeyType;
import ru.gpb.kms.entity.PurposeType;
import ru.gpb.kms.repository.KeyDataHistoryRepository;
import ru.gpb.kms.repository.KeyDataRepository;
import ru.gpb.kms.service.KeyDataService;
import ru.gpb.kms.service.KeyPassword;
import ru.gpb.kms.util.CertificateAndKeyStoreUtil;
import ru.gpb.kms.util.KeyGenerator;
import ru.gpb.kms.util.exceptions.InvalidArgumentApplicationException;
import ru.gpb.kms.util.exceptions.InvalidKeyApplicationException;
import ru.gpb.kms.util.exceptions.InvalidPasswordApplicationException;
import ru.gpb.kms.util.exceptions.KeyGenerationApplicationException;
import ru.gpb.kms.util.exceptions.KeyNotFoundApplicationException;
import ru.gpb.kms.util.exceptions.KeyPersistApplicationException;
import ru.gpb.kms.util.mapper.KeyDataMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Key Data service implementation class.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since 31.07.2023
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeyDataServiceImpl implements KeyDataService {

    private final KeyDataRepository keyDataRepository;
    private final KeyDataMapper keyDataMapper;
    private final KeyDataHistoryRepository keyDataHistoryRepository;
    private final Environment env;

    // TODO не будет работать в многоинстансовом окружении - следует заменить на адекватную реализацию.
    // хранение мапинга id ключа на пароль на время генерации мастер-ключа
    private static Map<UUID, KeyPassword> keyPassMap = new HashMap<>();
    // оперативное хранилище ключей (cache)
    private static Map<UUID, KeyDataDto> keyDataMap = new HashMap<>();

    // список атрибутов, которые можно менять, в зависимости от статуса
    private static HashMap<KeyStatus, HashSet<String>> attrMap = new HashMap<>();
    static {
        attrMap.put(KeyStatus.ENABLED, new HashSet<>(Arrays.asList("description", "expiryDate", "notifyDate", "relatedKey", "online")));
        attrMap.put(KeyStatus.DISABLED, new HashSet<>(Arrays.asList("description", "expiryDate", "notifyDate", "relatedKey", "online")));
        attrMap.put(KeyStatus.UNAVAILABLE, new HashSet<>(Arrays.asList("description", "expiryDate", "notifyDate", "relatedKey", "online")));
        attrMap.put(KeyStatus.PENDING_CREATION, new HashSet<>(Arrays.asList("alias", "description", "expiryDate", "algorithm", "notifyDate", "online")));
        attrMap.put(KeyStatus.PENDING_IMPORT, new HashSet<>(Arrays.asList("description", "expiryDate", "notifyDate", "online")));
        attrMap.put(KeyStatus.PENDING_DELETION, new HashSet<>(Arrays.asList("description", "notifyDate")));
    }

    // диаграмма переходов состояний
    private static HashMap<KeyStatus, HashSet<KeyStatus>> statusMap = new HashMap<>();
    static {
        statusMap.put(KeyStatus.NONE, new HashSet<>(Arrays.asList(KeyStatus.PENDING_CREATION, KeyStatus.ENABLED)));

        statusMap.put(KeyStatus.PENDING_CREATION, new HashSet<>(List.of(KeyStatus.ENABLED)));

        statusMap.put(KeyStatus.PENDING_IMPORT, new HashSet<>(Arrays.asList(KeyStatus.ENABLED, KeyStatus.UNAVAILABLE)));

        statusMap.put(KeyStatus.UNAVAILABLE, new HashSet<>(Arrays.asList(
                KeyStatus.ENABLED, KeyStatus.DISABLED, KeyStatus.PENDING_IMPORT,
                KeyStatus.PENDING_DELETION, KeyStatus.UNAVAILABLE)));

        statusMap.put(KeyStatus.ENABLED, new HashSet<>(Arrays.asList(
                KeyStatus.ENABLED, KeyStatus.DISABLED, KeyStatus.PENDING_IMPORT,
                KeyStatus.PENDING_DELETION, KeyStatus.UNAVAILABLE)));

        statusMap.put(KeyStatus.DISABLED, new HashSet<>(Arrays.asList(
                KeyStatus.ENABLED, KeyStatus.DISABLED, KeyStatus.PENDING_DELETION, KeyStatus.UNAVAILABLE)));

        statusMap.put(KeyStatus.PENDING_DELETION, new HashSet<>(List.of(KeyStatus.DISABLED)));
    }

    /**
     * Получение списка всех ключей.
     *
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<KeyDataDto> listAll() {
        log.info("listAll: <-");
        List<KeyData> list = keyDataRepository.findAll();
        log.info("listAll: -> " + Arrays.toString(list.toArray()));
        return list.stream().map(keyDataMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Возврат списка идентификаторов ключей в оперативном доступе.
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> listCache() {
        log.info("listCache: <-");
        return keyDataMap.values().stream().map(KeyDataDto::getId).collect(Collectors.toList());
    }

    /**
     * Обновление атрибутов ключа
     *
     * @param from
     */
    @Transactional
    @Override
    public void updateKeyData(String id, KeyDataDto from) {
        log.info("updateKeyData: <- id=" + id + ", key=" + from);

        UUID uid = UUID.fromString(id);
        KeyData to = keyDataRepository.findById(uid).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ '" + uid + "' не найден."));

        // обновление
        HashSet<String> attrs = attrMap.get(to.getStatus());
        if (attrs.contains("alias") && from.getAlias() != null) to.setAlias(from.getAlias());
        if (attrs.contains("description") && from.getDescription() != null) to.setDescription(from.getDescription());
        if (attrs.contains("expiryDate") && from.getExpiryDate() != null) to.setExpiryDate(from.getExpiryDate());
        if (attrs.contains("notifyDate") && from.getNotifyDate() != null) to.setNotifyDate(from.getNotifyDate());
        if (attrs.contains("relatedKey") && from.getRelatedKey() != null) {
            UUID uuid;
            try {
                uuid = UUID.fromString(from.getRelatedKey());
            } catch (IllegalArgumentException ex) {
                throw new InvalidArgumentApplicationException("Идентификатор связанного ключа '" + from.getRelatedKey() + "' не соответсвует формату UUID.");
            }
            KeyData r = keyDataRepository.findById(uuid).orElseThrow(
                    () -> new KeyNotFoundApplicationException("Связанный ключ " + from.getRelatedKey() + "не найден."));
            to.setRelatedKey(r);
        }
        if (attrs.contains("algorithm") && from.getAlgorithm() != null) to.setAlgorithm(from.getAlgorithm());
        if (attrs.contains("online")) to.setOnline(from.isOnline());

        // сохранение
        keyDataRepository.saveAndFlush(to);
    }

    /**
     * Удаление ключа (отметка об удалении).
     *
     * @param id
     */
    @Override
    @Transactional
    public void delete(String id) {
        log.info(String.format("delete: <- alias='%s'", id));
        UUID uuid = UUID.fromString(id);

        KeyData key = keyDataRepository.findById(uuid).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ для удаления '" + id + "' не найден."));
        changeStatus(key, KeyStatus.PENDING_DELETION);
    }

    // печать содержимого кэша для отладки
    private void printKeyMap() {
        for (UUID id : keyDataMap.keySet()) {
            log.trace("map: " + id + " => " + keyDataMap.get(id));
        }
    }

    /**
     * Получение секретного ключа шифрования данных в открытом виде.
     *
     * @param id
     * @return
     */
    @Override
    public SecretKey decodeDataKey(UUID id) {
        log.info("decodeDataKey: <- id=" + id);

        // найти ключ шифрования данных
        KeyData key = keyDataRepository.findById(id).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ шифрования данных '" + id + "' не найден."));

        // взять публичный мастер-ключ, указанный в ключе, из оперативного доступа
        // TODO можно исключить и брать данные из хранилища (но так быстрей)
        // printKeyMap();
        KeyDataDto pMaster = keyDataMap.get(key.getEncKey().getId());
        if (pMaster == null) {
            String msg = String.format("Мастер-ключ '%s' (публичная часть) для ключа шифрования данных в оперативном доступе не найден.", key.getEncKey().getId());
            log.warn("decodeDataKey: " + msg);
            throw new KeyNotFoundApplicationException(msg);
        }
        log.trace("decodeDataKey: в оперативном доступе найден публичный мастер-ключ " + pMaster.getId());

        // взять из оперативного доступа соответствующий связанный (секретный) ключ
        KeyDataDto sMaster = keyDataMap.get(UUID.fromString(pMaster.getRelatedKey()));
        if (sMaster == null) {
            String msg = String.format("Мастер-ключ '%s' (секретная часть) для ключа шифрования данных в оперативном доступе не найден.", key.getEncKey().getId());
            log.warn("decodeDataKey:" + msg);
            throw new KeyNotFoundApplicationException(msg);
        }
        log.trace("decodeDataKey: в оперативном доступе найден секретный мастер-ключ " + sMaster.getId());

        // Декодировать секретный ключ из мастера (KEK)
        PrivateKey privateKey = null;
        byte[] rawKey = Base64.getDecoder().decode(sMaster.getKey());
        try {
            privateKey = KeyGenerator.convertBytesToPrivateKey(rawKey, "RSA");
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            String msg = String.format("Ошибка восстановления приватного мастер-ключа '%s': %s", sMaster.getId(), e.getMessage());
            log.error(msg);
            throw new KeyGenerationApplicationException(msg);
        }
        // Расшифровать ключ шифрования данных
        try {
            byte[] data = Base64.getDecoder().decode(key.getKey());
            return KeyGenerator.decryptDataKey(data, privateKey);
        } catch (Exception ex) {
            String msg = String.format("Ошибка восстановления ключа шифрования данных '%s': %s", key.getId(), ex.getMessage());
            log.error("decodeDataKey:" + msg);
            throw new KeyNotFoundApplicationException(msg);
        }
    }

    /**
     * Генерация ключа шифрования данных
     *
     * @param alias - алиас ключа в хранилище.
     * @return KeyDataDto - сгенерированный ключ.
     */
    @Override
    @Transactional
    public KeyDataDto generateDataKey(String alias) {
        KeyDataDto master = null;

        log.info(String.format("createDataKey: <- alias='%s'", alias));

        // Проверить уникальность алиаса
        List<KeyData> aliases = keyDataRepository.findByAlias(alias);
        if (!aliases.isEmpty()) {
            log.warn("generateDataKey: Алиас '" + alias + "' уже использован");
            throw new InvalidArgumentApplicationException("Алиас '" + alias + "' уже использован ранее");
        }

        //
        // Найти подходящий мастер-ключ в оперативном хранилище
        //
        List<KeyData> keys = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(
                KeyType.PUBLIC, PurposeType.KEK, KeyStatus.ENABLED);
        for (KeyData key : keys) {
            log.debug("createDataKey: Проверка ключа " + key);
            if ((key.getExpiryDate() != null) && key.getExpiryDate().isAfter(LocalDateTime.now())) {
                // KeyData key = k;
                //
                // Взять публичный ключ из оперативного доступа
                //
                master = keyDataMap.get(key.getId());
                if (master == null) {
                    log.warn("Мастер-ключ " + key.getId() + " не загружен. Поиск следующего.");
                    // throw new KeyNotFoundApplicationException("Мастер-ключ " + key.getId() + " не загружен.");
                    continue; // пробуем другой ключ
                }
                // нашли загруженный ключ
                break;
            }
        }
        if (master == null) {
            String msg = "Не найден ни один актуальный загруженный мастер-ключ с неистекшим сроком действия.";
            log.warn(msg);
            throw new KeyNotFoundApplicationException(msg);
        } else {
            log.info("Найден подходящий загруженный мастер-ключ: " + master.getId());
        }

        // Получить публичный ключ шифрования ключей из мастера (KEK)
        PublicKey publicKey = null;
        byte[] rawKey = Base64.getDecoder().decode(master.getKey());
        try {
            publicKey = KeyGenerator.convertBytesToPublicKey(rawKey, "RSA");
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new KeyGenerationApplicationException("Ошибка создания ключа шифрования данных: " + e.getMessage());
        }

        // Зашифровать ключ шифрования данных
        KeyData keyData;
        try {
            SecretKey secretKey = KeyGenerator.generateDataKey();
            byte[] encryptedDataKey = KeyGenerator.encryptDataKey(secretKey, publicKey);
            keyData = new KeyData(alias, KeyGenerator.SYMMETRIC_MASTER_KEY_ALGORITHM,
                    KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.NONE);
            try {
                String years = env.getProperty("kms.key.ttl.DEK");
                keyData.setExpiryDate(LocalDateTime.now().plusYears(Long.parseLong(years)));
            } catch (Exception ex) {
                log.warn("Ошибка разбора настройки числа лет действия ключа данных kms.key.ttl.DEK (" + ex.getMessage() + "). Использовано значение по умолчанию - 10 лет.");
            }
            keyData.setDescription("Ключ шифрования данных " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            keyData.setKey(Base64.getEncoder().encodeToString(encryptedDataKey));
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new KeyGenerationApplicationException("Ошибка шифрования ключа шифрования данных: " + e.getMessage());
        }

        // Сохранить ссылку на ключ шифрования ключей (на мастер)
        keyData.setEncKey(keyDataRepository.findById(UUID.fromString(master.getId())).get());

        try {
            // Сохранить ключ шифрования данных
            keyDataRepository.save(keyData);
            log.info("Установка статуса для созданного ключа шифрования данных:" + keyData);
            // установить нужный статус
            changeStatus(keyData, KeyStatus.ENABLED);
            // keyDataRepository.save(keyData);
        } catch (Exception e) {
            throw new KeyPersistApplicationException("Ошибка сохранения ключа шифрования данных: " + e.getMessage());
        }
        return keyDataMapper.toDto(keyData);
    }

    /**
     * Загрузка публичного ключа.
     *
     * @param id идентификатор ключа
     * @return ключевая информация
     */
    @Override
    @Transactional(noRollbackFor = InvalidPasswordApplicationException.class)
    public KeyDataDto loadMasterKey(UUID id, char[] password) {
        log.info(String.format("loadMasterKey: <- id=%s", id));
        if (id == null) {
            throw new InvalidArgumentApplicationException("Пустой идентификатор ключа для загрузки.");
        }
        KeyData key = keyDataRepository.findById(id).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ошибка загрузки ключа: мастер-ключ '" + id + "' не найден."));

        if (key.getEncKey() != null) {
            throw new KeyNotFoundApplicationException("Ошибка загрузки ключа: ключ '" + id + "' не является мастер-ключем.");
        }

        if (key.getStatus() != KeyStatus.PENDING_IMPORT)
            return keyDataMapper.toDto(loadMasterKeyPhase1(key, password));
        else
            return keyDataMapper.toDto(loadMasterKeyPhase2(key, password));
    }

    /*
     * Первая фаза загрузки мастер-ключа (вызов для пользователя 1)
     */
    private KeyData loadMasterKeyPhase1(KeyData keyData, char[] password) {
        log.info("loadMasterKeyPhase1: <- ");

        // сохранение данных первого пользователя
        KeyPassword keyPassword = new KeyPassword();
        keyPassword.setKeyData(keyData);
        keyPassword.setPart1(password);
        keyPassword.setUser1(getUserInfo());

        // сохрание пароля в оперативном доступе
        keyPassMap.put(keyData.getId(), keyPassword);

        // установка статуса загрузки
        // TODO сделать это же и для связанного ключа?
        // keyData.setStatus(KeyStatus.PENDING_IMPORT);
        changeStatus(keyData, KeyStatus.PENDING_IMPORT);
        keyDataRepository.save(keyData);

        return keyData;
    }

    /*
     * Вторая фаза загрузки мастер-ключа (вызов для пользователя 2)
     */
    private KeyData loadMasterKeyPhase2(KeyData key, char[] password) {
        log.info("loadMasterKeyPhase2: <- " + key);

        // Проверка паролей
        KeyPassword keyPassword;
        try {
            keyPassword = checkPassword(key.getId(), password);
        } catch (InvalidPasswordApplicationException ex) {
            // проблема с паролями - ставим ключу статус "недоступен"
            // TODO скорей всего не сработает из-за обработки в рамках исключения
            changeStatus(key, KeyStatus.UNAVAILABLE);
            throw ex;
        }


        // Загрузить ключи из файла
        PrivateKey privateKey;
        PublicKey publicKey;
        try {
            String fileName = KeyGenerator.getFileNameFromURI(new URI(key.getKey()));
            privateKey = KeyGenerator.loadPrivateKey(fileName, key.getAlias(),
                    keyPassword.getPart1(), keyPassword.getPart2());
            log.info("loadMasterKeyPhase2: privateKey = " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            publicKey = KeyGenerator.loadPublicKey(fileName, key.getAlias(), keyPassword.getPart1());
            log.info("loadMasterKeyPhase2: publicKey = " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        } catch (URISyntaxException e) {
            throw new KeyNotFoundApplicationException("Ошибка загрузки ключа: " + e.getMessage());
        }

        //
        // Поместить пару ключей в оперативный доступ
        //
        KeyDataDto keyDto = keyDataMapper.toDto(key);
        keyDto.setKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        // поместить ключ в оперативный доступ
        keyDataMap.put(key.getId(), keyDto);
        log.info("loadMasterKeyPhase2: Загружен ключ " + key.getId());
        // установить ключу статус "Доступен"
        // key.setStatus(KeyStatus.ENABLED);
        changeStatus(key, KeyStatus.ENABLED);
        keyDataRepository.save(key);
        // проделать те же шаги для связанного ключа, если он есть
        KeyData relatedKeyData = key.getRelatedKey();
        if (relatedKeyData != null) {
            KeyDataDto relatedDto = keyDataMapper.toDto(relatedKeyData);
            relatedDto.setKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            // поместить связанный ключ в оперативный доступ
            keyDataMap.put(relatedKeyData.getId(), relatedDto);
            log.info("loadMasterKeyPhase2: Загружен ключ " + relatedKeyData.getId());
            // установить ключу статус "Доступен"
            // relatedKeyData.setStatus(KeyStatus.ENABLED);
            changeStatus(relatedKeyData, KeyStatus.ENABLED);
            keyDataRepository.save(relatedKeyData);
        }

        return key;
    }

    /*
     * Проверка ограничений паролей и пользователей
     * и преобразование паролей для дальнейшего использования.
     */
    private KeyPassword checkPassword(UUID id, char[] password) {

        // Иначе происходит поиск сущности Пароль КИ, связанной с текущей [КК_KMS] Ключевая информация (КИ).
        KeyPassword keyPassword = keyPassMap.get(id);

        // Если выполнено любое из условий:
        // либо Пароль КИ не найден
        // либо Пароль КИ.Часть 1 пуст
        // либо Пароль КИ.Пользователь 1 равен идентификатору текущего пользователя
        if (password == null || password.length == 0) {
            String msg = "Второй пароль не может быть пустым";
            log.error("checkPassword: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }
        if (keyPassword == null) {
            String msg = String.format("Пароль для ключа id='%s' не загружен.", id);
            log.error("checkPassword: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }
        if (keyPassword.getPart1() == null) {
            String msg = String.format("Пароль первого пользователя для id='%s' не может быть пустым.", id);
            log.error("checkPassword: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }
        if (keyPassword.getUser1() == null) {
            String msg = "Пользователь 1 не может быть пустым.";
            log.error("checkPassword: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }

        if (keyPassword.getUser1().equals(getUserInfo())) {
            String msg = "Пользователь 1 совпадает с Пользователем 2.";
            log.error("checkPassword: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }

        // реквизит Пароль КИ.Часть 2 устанавливается в значение <пароль> и реквизит Пароль КИ.Пользователь 2
        // устанавливается равным идентификатору текущего пользователя.
        keyPassword.setUser2(getUserInfo());
        keyPassword.setPart2(password);

        //
        // преобразование паролей пользвателя 1 и пользователя 2 через XOR
        //
        int maxLength = Math.max(keyPassword.getPart1().length, keyPassword.getPart2().length);
        char[] result = new char[maxLength];

        for (int i = 0; i < maxLength; i++) {
            char value1 = (i < keyPassword.getPart1().length) ? keyPassword.getPart1()[i] : 0;
            char value2 = (i < keyPassword.getPart2().length) ? keyPassword.getPart2()[i] : 0;
            result[i] = (char) (value1 ^ value2);
        }

        // установка паролей в новые значения
        keyPassword.setPart1(new char[result.length / 2]);
        System.arraycopy(result, 0, keyPassword.getPart1(), 0, keyPassword.getPart1().length);
        keyPassword.setPart2(new char[result.length - keyPassword.getPart1().length]);
        System.arraycopy(result, 0 + keyPassword.getPart1().length, keyPassword.getPart2(), 0, keyPassword.getPart2().length);

        // for (int i = 0; i < keyPassword.getPart1().length; i++) {
        //     keyPassword.getPart1()[i] = result[i];
        // }
        // keyPassword.setPart2(new char[result.length - keyPassword.getPart1().length]);
        // for (int i = 0; i < keyPassword.getPart2().length; i++) {
        //     keyPassword.getPart2()[i] = result[i + keyPassword.getPart1().length];
        // }

        return keyPassword;
    }

    /**
     * Генерация мастер-ключа.
     *
     * @param id             Идентификатор (необязательный)
     * @param alias          Алиас (необязательный)
     * @param desc           Описание (необязательный)
     * @param expirationDate Срок действия (необязательный)
     * @param password       Пароль
     * @param notifyDate     Срок уведомления об истечении срока действия (необязательный)
     * @return
     */
    @Transactional
    @Override
    public KeyDataDto generateMasterKey(UUID id, String alias, String desc, LocalDateTime expirationDate,
                                        char[] password, LocalDateTime notifyDate) {
        log.info(String.format("generateMasterKey: <- id=%s, alias='%s', desc='%s', expirationDate=%s, notifyDate=%s",
                id, alias, desc, expirationDate, notifyDate));

        if (id == null) {
            // Проверить уникальность алиаса
            List<KeyData> aliases = keyDataRepository.findByAlias(alias);
            if (!aliases.isEmpty()) {
                log.warn("generateMasterKey: Алиас '" + alias + "' уже использован");
                throw new InvalidArgumentApplicationException("Алиас '" + alias + "' уже использован ранее");
            }
            return keyDataMapper.toDto(generateMasterKeyPhase1(alias, desc, expirationDate, password, notifyDate));
        } else {
            return keyDataMapper.toDto(generateMasterKeyPhase2(id, alias, desc, expirationDate, password, notifyDate));
        }
    }

    /*
     * Вызов для пользователя 1
     */
    private KeyData generateMasterKeyPhase1(String alias, String desc, LocalDateTime expirationDate,
                                            char[] password, LocalDateTime notifyDate) {
        log.info("generateMasterKeyPhase1: <- ");

        KeyData keyData = new KeyData(alias, KeyGenerator.ASYMMETRIC_MASTER_KEY_ALGORITHM,
                KeyType.PRIVATE, PurposeType.KEK, KeyStatus.NONE);
        keyData.setDescription(Objects.requireNonNullElseGet(desc, () -> "Мастер-ключ (приватная часть) " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        // взять время из настройки.
        if (expirationDate == null) {
            try {
                String years = env.getProperty("kms.key.ttl.KEK");
                keyData.setExpiryDate(LocalDateTime.now().plusYears(Long.parseLong(years)));
            } catch (Exception ex) {
                log.warn("generateMasterKeyPhase1: Неверный формат настройки kms.key.ttl.KEK. Установлено по умолчанию - 10 лет.");
                keyData.setExpiryDate(LocalDateTime.now().plusYears(10));
            }
        } else {
            keyData.setExpiryDate(expirationDate);
        }
        // keyData.setExpiryDate(Objects.requireNonNullElseGet(expirationDate, () -> LocalDateTime.now().plusYears(5)));

        // сохранение ключа для получения идентификатора
        keyDataRepository.saveAndFlush(keyData);
        changeStatus(keyData, KeyStatus.PENDING_CREATION);

        // keyDataRepository.save(keyData);

        // сохранение данных первого пользователя
        KeyPassword keyPassword = new KeyPassword();
        keyPassword.setKeyData(keyData);
        keyPassword.setPart1(password);
        keyPassword.setUser1(getUserInfo());

        // сохрание пароля в оперативном доступе
        keyPassMap.put(keyData.getId(), keyPassword);
        log.info("generateMasterKeyPhase1: -> keyData=" + keyData + ", keyPassword=" + keyPassword);
        return keyData;
    }

    /*
     * Вызов для пользователя 2
     */
    private KeyData generateMasterKeyPhase2(UUID id, String alias, String desc, LocalDateTime expirationDate,
                                            char[] password, LocalDateTime notifyDate) {
        log.info("generateMasterKeyPhase2: <- ");

        //
        // поиск записи, для которой происходит генерация ключа
        //
        KeyData keyData = keyDataRepository.findById(id).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ с идентификатором '" + id + "' не найден."));


        if ((!keyData.getPurposeType().equals(PurposeType.KEK)) ||
                (!keyData.getStatus().equals(KeyStatus.PENDING_CREATION)) ||
                (keyData.getExpiryDate().compareTo(LocalDateTime.now()) < 0)) {
            String msg = String.format("Ключ '%s' не подходит для продолжения генерации Мастер-ключа (тип назначения, статус или срок действия).", keyData);
            log.error("generateMasterKeyPhase2: " + msg);
            throw new InvalidKeyApplicationException(msg);
        }

        // Проверка паролей
        KeyPassword keyPassword = checkPassword(id, password);

        // Происходит создание ключа с алгоритмом равным "PBKDF2WithHmacSHA512" и реквизитами
        // Пароль КИ.Часть 1 и Пароль КИ.Часть 2

        //
        // создать пару публичный/приватный мастер-ключа (ассиметричный)
        //
        KeyPair keyPair;
        String fileName;
        try {
            // Генерация приватного и публичного ключа с алгоритмом RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyGenerator.ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

            // Сохранение пары ключей в файловое хранилище ключей
            String storeURI = env.getProperty("kms.storeURI"); // каталог хранилища ключей (настройка)
            if (KeyGenerator.isValidURI(storeURI)) {
                // String storeName = KeyGenerator.getFileNameFromURI(new URI(storeURI));
                // генерация имени файла
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                fileName = currentDateTime.format(formatter) + ".jks";
                String storeName = KeyGenerator.getFileNameFromURI(new URI(storeURI)) + "/" + fileName;
                log.info("generateMasterKeyPhase2: store to file '" + storeName + "'");
                CertificateAndKeyStoreUtil.storeKeyPair(keyPair, storeName, alias,
                        keyPassword.getPart1(), keyPassword.getPart2());
            } else {
                log.error("generateMasterKeyPhase2: Invalid URI string properties '" + storeURI + "'");
                throw new KeyPersistApplicationException("Can't save key: Invalid URI string properties '" + storeURI + "'");
            }

        } catch (Exception ex) {
            String msg = String.format("Ошибка создания Мастер-ключа: %s", ex.getMessage());
            log.error("generateMasterKeyPhase2: " + msg);
            keyPassMap.remove(id);
            throw new KeyGenerationApplicationException(msg);
        }

        // очищаются все атрибуты Пароль КИ
        keyPassMap.remove(id);
        // Обновление данных
        keyData.setCreatedDate(LocalDateTime.now());
        keyData.setDescription(desc);
        keyData.setNotifyDate(notifyDate);

        //
        // [КК_KMS] Ключевая информация (КИ) сохраняется в хранилище ключей: сохранение URI в ключ
        //
        try {
            // Преобразуем в URI и записываем вместо ключа
            keyData.setKey(Paths.get(fileName).toUri().toString());
            // сохранить секретный ключ
            keyDataRepository.save(keyData);
            // сохранить историю секретного ключа
            changeStatus(keyData, KeyStatus.ENABLED);

            if (keyData.getKeyType() != KeyType.SYMMETRIC) {
                //
                // создать запись для публичного ключа
                //
                KeyData publicKeyData = new KeyData(keyData.getAlias(), keyData.getAlgorithm(), KeyType.PUBLIC,
                        keyData.getPurposeType(), keyData.getStatus());
                if (desc == null || desc.isEmpty()) {
                    publicKeyData.setDescription("Мастер-ключ (публичная часть) " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } else {
                    publicKeyData.setDescription(desc.replaceFirst("приватная", "публичная"));
                }

                // скопировать URI ссылки на хранилище ключей
                publicKeyData.setKey(keyData.getKey());

                // установить ссылку секретного ключа на публичный
                publicKeyData.setRelatedKey(keyData);
                // сохранить ссылку публичного ключа на секретный
                keyDataRepository.save(publicKeyData);
                // установить ссылку секретного ключа на публичный
                keyData.setRelatedKey(publicKeyData);
                // обновить секретный ключ
                keyDataRepository.save(keyData);
                // сохранить историю публичного ключа
                changeStatus(publicKeyData, KeyStatus.ENABLED);

                // Поместить публичный ключ в оперативный доступ
                KeyDataDto publicDto = keyDataMapper.toDto(publicKeyData);
                publicDto.setKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
                keyDataMap.put(publicKeyData.getId(), publicDto);
                log.info("generateMasterKeyPhase2: Загружен ключ " + publicKeyData.getId());
            }
            // Поместить секретный ключ в оперативный доступ
            KeyDataDto secretDto = keyDataMapper.toDto(keyData);
            secretDto.setKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            keyDataMap.put(keyData.getId(), secretDto);
            log.info("generateMasterKeyPhase2: Загружен ключ " + keyData.getId());

        } catch (Exception ex) {
            String msg = String.format("Ошибка сохранения мастер-ключа: %s", ex.getMessage());
            log.error("generateMasterKeyPhase2: " + msg);
            throw new KeyPersistApplicationException(msg);
        }

        // успех создания мастер-ключа
        log.info("generateMasterKeyPhase2: -> sec=" + keyData);
        return keyData;
    }

    /*
     * обновить историю статусов
     */
    private void changeStatus(KeyData key, KeyStatus newStatus) {
        log.info(String.format("changeStatus: <- key = '%s', new status = '%s'", key, newStatus));

        // проверяем, что можно переходить в указанный статус
        if (statusMap.get(key.getStatus()).contains(newStatus)) {
            key.setStatus(newStatus);
        } else {
            throw new InvalidArgumentApplicationException("Переход из статуса " + key.getStatus() + " в статус " + newStatus + " запрещен.");
        }
        // создаем запись истории
        KeyDataHistory history = KeyDataHistory.builder()
                .createdDate(LocalDateTime.now())
                .status(newStatus)
                .user(getUserInfo())
                .userType("USER")
                .key(key)
                .build();
        keyDataHistoryRepository.save(history);
        // добавить в историю статусов
        key.getHistory().add(history);

        // установить текущий статус для ключа
        key.setStatus(newStatus);
        keyDataRepository.save(key);

        //
        // если ключ ассиметричный, меняем статус и для связанного ключа
        //
        if (key.getKeyType() != KeyType.SYMMETRIC && key.getRelatedKey() != null) {
            log.info("changeStatus: смена статусе для ключа, связанного c ключом '" + key.getId() + "'");
            key = key.getRelatedKey();
            // создаем запись истории
            history = KeyDataHistory.builder()
                    .createdDate(LocalDateTime.now())
                    .status(newStatus)
                    .user(getUserInfo())
                    .userType("USER")
                    .key(key)
                    .build();
            keyDataHistoryRepository.save(history);
            // добавить в историю статусов
            key.getHistory().add(history);

            // установить текущий статус для ключа
            key.setStatus(newStatus);
            keyDataRepository.save(key);
        }
    }

    /**
     * Смена статуса ключа.
     *
     * @param id        ключа.
     * @param newStatus новый статус, в который требуется переход.
     */
    @Transactional
    public void changeStatus(String id, KeyStatus newStatus) {
        log.info(String.format("changeStatus: <- id = '%s', status = '%s'", id, newStatus));

        UUID uid;
        try {
            uid = UUID.fromString(id);
        } catch (Exception ex) {
            throw new InvalidArgumentApplicationException("Некорректный формат идентификатора ключа для смены статуса: " + id);
        }
        KeyData key = keyDataRepository.findById(uid).orElseThrow(
                () -> new KeyNotFoundApplicationException("Ключ с идентификатором '" + uid + "' для смены статуса не найден."));
        changeStatus(key, newStatus);
    }

    /**
     * Перешифровка ключей шифрования данных (DEK), зашифрованных мастер-ключем (KEK), отмеченным для удаления.
     */
    public void rotateDataKey() {

        boolean isReady = false;

        log.info("rotateDataKey: <-.");

        //
        // Проверить, что в оперативном доступе есть хотя бы один мастер-ключ в статусе разрешен
        // (будет чем перешифровать?)
        //
        List<KeyData> ms = keyDataRepository.findByPurposeTypeAndStatus(PurposeType.KEK, KeyStatus.ENABLED);
        for (KeyData m : ms) {
            // нашли разрешенного мастера, загруженного в оперативный доступ
            if (keyDataMap.containsKey(m.getId()) && m.getEncKey() == null) {
                isReady = true;
                break;
            }
        }
        if (!isReady) {
            log.warn("rotateDataKey: В оперативном доступе нет ни одного мастер-ключа в статусе Разрешен. Перешифровка недоступна.");
            return;
        }

        //
        // Цикл по всем мастер-ключам, подлежащим удалению
        //
        List<KeyData> masters = keyDataRepository.findByPurposeTypeAndStatus(PurposeType.KEK, KeyStatus.PENDING_DELETION);
        if (masters.isEmpty()) {
            log.info("rotateDataKey: Мастер-ключей, отмеченных для удаления, не найдено.");
        }
        for (KeyData m : masters) {
            log.info("rotateDataKey: анализиуем KEK '" + m.getId() + "'");
            if (!keyDataMap.containsKey(m.getId())) {
                log.info("rotateDataKey: KEK '" + m.getId() + "' в оперативном доступе не найден.");
                continue; // переход к следующему DEK
            }
            // взять все ключи, зашифрованные удаляемым мастер-ключем
            List<KeyData> deks = keyDataRepository.findByEncKey(m);
            // по каждому из найденных DEK
            for (KeyData dek : deks) {
                log.debug("rotateDataKey: проверяем возможность перешифровки DEK '" + dek.getId() + "'");
                // найти список мастер-ключей в статусе Разрешен (кандидаты для шифрования)
                List<KeyData> keks = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(KeyType.PUBLIC, PurposeType.KEK, KeyStatus.ENABLED);
                for (KeyData kek : keks) {
                    //
                    // попытка перешифровать каким-либо мастер-ключем, доступным в оперативном доступе
                    //
                    if (keyDataMap.containsKey(kek.getId())) {
                        // целевой мастер-ключ доступен в оперативном доступе
                        log.info("rotateDataKey: KEK кандидат '" + kek.getId() + "'");
                        try {
                            // если kek находится в оперативном доступе
                            log.debug("rotateDataKey: Попытка перешифрации DEK '" + dek.getId() + "' с KEK '" + kek.getId() + "'");
                            SecretKey secretDek = decodeDataKey(dek.getId());
                            log.debug("rotateDataKey: DEK '" + dek.getId() + "' расшифрован.");
                            PublicKey publicKek = KeyGenerator.convertBytesToPublicKey(
                                    Base64.getDecoder().decode(keyDataMap.get(kek.getId()).getKey()), "RSA");
                            // перешифровать dek новым kek
                            byte[] data = KeyGenerator.encryptDataKey(secretDek, publicKek);
                            // установить в новое значение
                            dek.setKey(Base64.getEncoder().encodeToString(data));
                            // указать ссылку на новый kek
                            dek.setEncKey(kek);
                            // сохранить dek
                            keyDataRepository.save(dek);
                            log.info("rotateDataKey: DEK '" + dek.getId() + "' перешифрован с KEK '" + kek.getId() + "'");
                            break; // выход из цикла перешифрации конкретного DEK
                        } catch (Exception ex) {
                            log.error(String.format("rotateDataKey: Ошибка перешифрации DEK '%s' c KEK '%s': %s", dek.getId(), kek.getId(), ex.getMessage()));
                            ex.printStackTrace();
                            // ignore - переход к следующему KEK для новой попытки
                        }
                    } else {
                        log.info("rotateDataKey: KEK '" + kek.getId() + "' в оперативном доступе не найден.");
                    }
                }
            } // завершение перебора dek
        }
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
