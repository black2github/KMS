package ru.gazprombank.token.kms.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyStatus;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.repository.KeyDataRepository;
import ru.gazprombank.token.kms.service.InvalidArgumentApplicationException;
import ru.gazprombank.token.kms.service.InvalidKeyApplicationException;
import ru.gazprombank.token.kms.service.InvalidPasswordApplicationException;
import ru.gazprombank.token.kms.service.KeyDataService;
import ru.gazprombank.token.kms.service.KeyGenerationApplicationException;
import ru.gazprombank.token.kms.service.KeyNotFoundApplicationException;
import ru.gazprombank.token.kms.service.KeyPassword;
import ru.gazprombank.token.kms.service.KeyPersistApplicationException;
import ru.gazprombank.token.kms.service.SecurityApplicationException;
import ru.gazprombank.token.kms.util.CertificateAndKeyStoreUtil;
import ru.gazprombank.token.kms.util.KeyGenerator;
import ru.gazprombank.token.kms.util.mapper.KeyDataMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyDataServiceImpl implements KeyDataService {

    private final KeyDataRepository keyDataRepository;
    private final KeyDataMapper keyDataMapper;

    //private final KeyDataMapper keyDataMapper;

    // хранение мапинга id ключа на пароль на время генерации мастер-ключа
    private static Map<UUID, KeyPassword> keyPassMap = new HashMap<>();
    // оперативное хранилище ключей
    private static Map<UUID, KeyDataDto> keyDataMap = new HashMap<>();

    // файл хранилища ключей (настройка)
    @Value("${kms.storeURI}")
    private String storeURI;

    @Transactional
    @Override
    public List<KeyData> listAll() {
        log.debug("listAll: <-");
        List<KeyData> list = new LinkedList<>();
        Iterable<KeyData> it = keyDataRepository.findAll();
        for (KeyData keyData : it) {
            list.add(keyData);
        }
        log.debug("listAll: -> " + Arrays.toString(list.toArray()));
        return list;
    }

    @Transactional
    @Override
    public void updateKeyData(KeyDataDto keyDataDto) {

    }

    @Transactional
    @Override
    public KeyData saveKeyData(KeyDataDto keyDataDto) {
        return null;
    }

    @Override
    public void delete(KeyDataDto keyDataDto) {

    }

    @Override
    @Transactional
    public KeyData createDataKey(String alias) {
        log.info(String.format("createDataKey: <- alias='%s'", alias));
        //
        // Найти подходящий мастер-ключ в оперативном хранилище
        //
        List<KeyData> keys = keyDataRepository.findByKeyTypeAndPurposeTypeAndStatus(
                KeyType.PUBLIC, PurposeType.KEK, KeyStatus.ENABLED);
        KeyDataDto master = null;
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
        if (master==null) {
            String msg = "Не найден ни один актуальный мастер-ключ с неистекшим сроком действия.";
            log.warn(msg);
            throw new KeyNotFoundApplicationException(msg);
        }

        // Получить публичный ключ шифрования ключей (KEK)
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
                    KeyType.SYMMETRIC, PurposeType.DEK, KeyStatus.ENABLED);
            keyData.setDescription("Ключ шифрования данных " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            keyData.setKey(Base64.getEncoder().encodeToString(encryptedDataKey));
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new KeyGenerationApplicationException("Ошибка шифрования ключа шифрования данных: " + e.getMessage());
        }

        // Сохранить ключ шифрования данных
        try {
            keyDataRepository.save(keyData);
        } catch (Exception e) {
            throw new KeyPersistApplicationException("Ошибка сохранения ключа шифрования данных: " + e.getMessage());
        }
        return keyData;
    }

    /**
     * Загрузка публичного ключа.
     *
     * @param id идентификатор ключа
     * @return ключевая информация
     */
    @Transactional
    @Override
    public KeyData loadMasterKey(UUID id, char[] password) {
        log.info(String.format("loadMasterKey: <- id=%s", id));
        if (id == null) {
            throw new InvalidArgumentApplicationException("Пустой идентификатор ключа для загрузки.");
        }
        KeyData key = keyDataRepository.findById(id).orElse(null);;
        // key = keyDataRepository.getReferenceById(id);
        if (key == null || key.getEncKey() != null) {
            throw new KeyNotFoundApplicationException("Ошибка загрузки ключа: мастер-ключ " + id + " не найден.");
        }

        if (key.getStatus() != KeyStatus.PENDING_IMPORT)
            return loadMasterKeyPhase1(key, password);
        else
            return loadMasterKeyPhase2(key, password);
    }

    /*
     * Вызов для пользователя 1
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
        keyData.setStatus(KeyStatus.PENDING_IMPORT);
        keyDataRepository.save(keyData);

        return keyData;
    }

    /*
     * Вызов для пользователя 2
     */
    private KeyData loadMasterKeyPhase2(KeyData key, char[] password) {
        log.info("loadMasterKeyPhase2: <- " + key);

        // Проверка паролей
        KeyPassword keyPassword = checkPassword(key.getId(), password);

        // Загрузить ключи из файла
        PrivateKey privateKey;
        PublicKey publicKey;
        try {
            String fileName = KeyGenerator.getFileNameFromURI(new URI(key.getKey()));
            privateKey = KeyGenerator.loadPrivateKey(fileName, key.getAlias(),
                    keyPassword.getPart1(), keyPassword.getPart2());
            publicKey = KeyGenerator.loadPublicKey(fileName, key.getAlias(), keyPassword.getPart1());
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                 URISyntaxException| IOException e) {
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
        key.setStatus(KeyStatus.ENABLED);
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
            relatedKeyData.setStatus(KeyStatus.ENABLED);
            keyDataRepository.save(relatedKeyData);
        }

        return key;
    }

    /*
     * Проверка ограничений пароля.
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
            String msg = String.format("Пароль для id='%s' не найден.", id);
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
        keyPassword.setPart2(password);
        keyPassword.setUser2(getUserInfo());

        return keyPassword;
    }

    @Transactional
    @Override
    public KeyData generateMasterKey(UUID id, String alias, String desc, LocalDateTime expirationDate,
                                     char[] password, LocalDateTime notifyDate) {
        log.info(String.format("generateMasterKey: <- id=%s, alias='%s', desc='%s', expirationDate=%s, notifyDate=%s",
                id, alias, desc, expirationDate, notifyDate));

        if (id == null)
            return generateMasterKeyPhase1(alias, desc, expirationDate, password, notifyDate);
        else
            return generateMasterKeyPhase2(id, alias, desc, expirationDate, password, notifyDate);
    }

    /*
     * Вызов для пользователя 1
     */
    private KeyData generateMasterKeyPhase1(String alias, String desc, LocalDateTime expirationDate,
                                            char[] password, LocalDateTime notifyDate) {
        log.info("generateMasterKeyPhase1: <- ");

        KeyData keyData = new KeyData(alias, KeyGenerator.ASYMMETRIC_MASTER_KEY_ALGORITHM,
                KeyType.PRIVATE, PurposeType.KEK, KeyStatus.PENDING_CREATION);
        keyData.setDescription(Objects.requireNonNullElseGet(desc, () -> "Мастер-ключ (приватная часть) " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        // TODO - брать значение настройки
        keyData.setExpiryDate(Objects.requireNonNullElseGet(expirationDate, () -> LocalDateTime.now().plusYears(5)));
        keyDataRepository.save(keyData);

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

        KeyData keyData;
        try {
            keyData = keyDataRepository.getReferenceById(id);
        } catch (Exception ex) {
            log.error("generateMasterKeyPhase2: " + ex);
            throw new KeyGenerationApplicationException(ex.getMessage());
        }

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

        // создать пару публичный/приватный мастер-ключа (ассиметричный)
        KeyPair keyPair;
        String fileName;
        try {
            // Генерация приватного и публичного ключа с алгоритмом RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyGenerator.ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

            // Сохранение пары ключей в файловое хранилище ключей
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
        changeStatus(keyData.getId(), KeyStatus.ENABLED);
        keyData.setCreatedDate(LocalDateTime.now());

        //
        // [КК_KMS] Ключевая информация (КИ) сохраняется в хранилище ключей: сохранение URI в ключ
        //
        try {
            // Преобразуем в URI и записываем вместо ключа
            keyData.setKey(Paths.get(fileName).toUri().toString());
            // сохранить секретный ключ
            keyDataRepository.save(keyData);

            if (keyData.getKeyType() != KeyType.SYMMETRIC) {
                //
                // создать запись для публичного ключа
                //
                KeyData publicKeyData = new KeyData(keyData.getAlias(), keyData.getAlgorithm(), KeyType.PUBLIC,
                        keyData.getPurposeType(), keyData.getStatus());
                publicKeyData.setDescription(Objects.requireNonNullElseGet(desc.replaceAll("приватная", "публичная"),
                        () -> "Мастер-ключ (публичная часть) " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

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
     *
     */
    private void changeStatus(UUID id, KeyStatus keyStatus) {
        log.info(String.format("changeStatus: <- new status = '%s', user = '%s'", keyStatus, getUserInfo()));
        // TODO
        KeyData keyData = keyDataRepository.getReferenceById(id);
        keyData.setStatus(keyStatus);
        keyDataRepository.save(keyData);
    }

    /*
     *
     */
    private UserDetails getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        }
        return null;
    }
}
