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
import ru.gazprombank.token.kms.service.InvalidKeyApplicationException;
import ru.gazprombank.token.kms.service.InvalidPasswordApplicationException;
import ru.gazprombank.token.kms.service.KeyDataService;
import ru.gazprombank.token.kms.service.KeyGenerationApplicationException;
import ru.gazprombank.token.kms.service.KeyPassword;
import ru.gazprombank.token.kms.service.KeyPersistApplicationException;
import ru.gazprombank.token.kms.util.CertificateAndKeyStoreUtil;
import ru.gazprombank.token.kms.util.KeyGenerator;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyDataServiceImpl implements KeyDataService {

    private final KeyDataRepository keyDataRepository;
    //private final KeyDataMapper keyDataMapper;

    // хранение мапинга id ключа на пароль на время генерации мастер-ключа
    private static Map<UUID, KeyPassword> keyPassMap = new HashMap<>();
    // оперативное хранилище ключей
    private static Map<UUID, KeyData> keyDataMap = new HashMap<>();

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
        keyData.setDescription(Objects.requireNonNullElseGet(desc, () -> "Мастер-ключ " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        // TODO - брать значение настройки
        keyData.setExpiryDate(Objects.requireNonNullElseGet(expirationDate, () -> LocalDateTime.now().plusSeconds(3600)));
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
        // Иначе происходит поиск сущности Пароль КИ, связанной с текущей [КК_KMS] Ключевая информация (КИ).
        KeyPassword keyPassword = keyPassMap.get(id);

        // Если выполнено любое из условий:
        // либо Пароль КИ не найден
        // либо Пароль КИ.Часть 1 пуст
        // либо Пароль КИ.Пользователь 1 равен идентификатору текущего пользователя
        if (password == null || password.length == 0) {
            String msg = "Второй пароль не может быть пустым";
            log.error("generateMasterKeyPhase2: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }
        if (keyPassword == null) {
            String msg = String.format("Пароль для id='%s' не найден.", id);
            log.error("generateMasterKeyPhase2: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }
        if (keyPassword.getPart1() == null) {
            String msg = String.format("Пароль первого пользователя для id='%s' не может быть пустым.", id);
            log.error("generateMasterKeyPhase2: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }
        if (keyPassword.getUser1() == null) {
            String msg = "Пользователь 1 не может быть пустым.";
            log.error("generateMasterKeyPhase2: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }

        if (keyPassword.getUser1().equals(getUserInfo())) {
            String msg = "Пользователь 1 совпадает с Пользователем 2.";
            log.error("generateMasterKeyPhase2: " + msg);
            throw new InvalidPasswordApplicationException(msg);
        }

        // реквизит Пароль КИ.Часть 2 устанавливается в значение <пароль> и реквизит Пароль КИ.Пользователь 2
        // устанавливается равным идентификатору текущего пользователя.
        keyPassword.setPart2(password);
        keyPassword.setUser2(getUserInfo());

        // Происходит создание ключа с алгоритмом равным "PBKDF2WithHmacSHA512" и реквизитами
        // Пароль КИ.Часть 1 и Пароль КИ.Часть 2

        // создать пару публичный/приватный мастер-ключа (ассиметричный)
        KeyPair keyPair;
        try {
            // Генерация приватного и публичного ключа с алгоритмом RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyGenerator.ASYMMETRIC_MASTER_KEY_ALGORITHM_TYPE);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

            // Сохранение пары ключей в файловое хранилище ключей
            CertificateAndKeyStoreUtil.storeKeyPair(keyPair, storeURI, alias,
                    keyPassword.getPart1(), keyPassword.getPart2());

        } catch (Exception ex) {
            String msg = String.format("Ошибка создания Мастер-ключа: %s", ex.getMessage());
            log.error("generateMasterKeyPhase2: " + msg);
            keyPassMap.remove(id);
            throw new KeyGenerationApplicationException(msg);
        }

        // очищаются все атрибуты Пароль КИ
        keyPassMap.remove(id);
        // Обновление данных данных
        changeStatus(keyData.getId(), KeyStatus.ENABLED);
        keyData.setCreatedDate(LocalDateTime.now());

        // [КК_KMS] Ключевая информация (КИ) сохраняется в хранилище ключей: сохранение URI в ключ
        if (KeyGenerator.isValidURI(storeURI)) {
            keyData.setKey(storeURI);
        } else {
            String msg = String.format("Некорректный URI: '%s'", storeURI);
            log.error("generateMasterKeyPhase2: " + msg);
            throw new KeyPersistApplicationException(msg);
        }

        try {
            keyDataRepository.save(keyData);
        } catch (Exception ex) {
            String msg = String.format("Ошибка сохранения мастер-ключа: %s", ex.getMessage());
            log.error("generateMasterKeyPhase2: " + msg);
            throw new KeyPersistApplicationException(msg);
        }

        // успех создания мастер-ключа
        log.info("generateMasterKeyPhase2: -> " + keyData);
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
