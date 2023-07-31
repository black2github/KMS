package ru.gazprombank.token.kms.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.service.KeyDataService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Работа с ключами.
 */
@RestController
@RequestMapping("/keys/")
@RequiredArgsConstructor
public class KeyDataController {

    private static final Logger log = LoggerFactory.getLogger(KeyDataController.class);

    private final KeyDataService keyDataService;

    /**
     * Получение списка всех ключей.
     *
     * @return
     */
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KeyDataDto>> list() {
        log.info("list: <- ");
        return new ResponseEntity<>(keyDataService.listAll(), HttpStatus.OK);
    }

    /**
     * Получение списка всех ключей.
     *
     * @return
     */
    @GetMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listCache() {
        log.info("list: <- ");
        return new ResponseEntity<>(keyDataService.listCache(), HttpStatus.OK);
    }

    /**
     * Генерация мастер-ключа через POST JSON запроса и пароля.
     *
     * @param formData
     * @return
     */
    @PostMapping(path = "/master", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyDataDto> generateMasterKey(@RequestBody MasterKeyGenerationRequest formData) {
        log.info(String.format("generateMasterKey: <- key="+formData.getKey()));

        char[] password = (formData.getPassword() == null || formData.getPassword().isEmpty())
                ? null : formData.getPassword().toCharArray();

        // генерация мастер-ключа
        UUID id = (formData.getKey().getId() == null) ? null : UUID.fromString(formData.getKey().getId());
        KeyDataDto keyData = keyDataService.generateMasterKey(id, formData.getKey().getAlias(),
                formData.getKey().getDescription(), formData.getKey().getExpiryDate(), password, formData.getKey().getNotifyDate());

        log.debug("generateMasterKey: -> " + keyData);

        return new ResponseEntity<>(keyData, HttpStatus.OK);
    }

    /**
     * Генерация ключа через POST данных формы.
     *
     * @param formData
     * @return
     */
    @PostMapping(path = "/master3", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyDataDto> generateMasterKey3(@RequestBody MultiValueMap<String, String> formData) {
        log.info(String.format("generateMasterKey3: <- " + formData.toSingleValueMap()));

        Map<String, String> valueMap = formData.toSingleValueMap();

        String value = valueMap.get("id");
        UUID id = (value == null || value.isEmpty()) ? null : UUID.fromString(value);
        value = valueMap.get("password");
        char[] password = (value == null) ? null : value.toCharArray();
        value = valueMap.get("alias");
        String alias = (value == null || value.isEmpty()) ? null : value;
        value = valueMap.get("description");
        String desc = (value == null || value.isEmpty()) ? null : value;
        value = valueMap.get("expirationDate");
        LocalDateTime expirationDate = (value == null || value.isEmpty()) ? null :
                LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); //2028-07-26T19:40:22.7440662
        value = valueMap.get("algorithm");
        String algorithm = (value == null || value.isEmpty()) ? null : value;
        value = valueMap.get("notifyDate");
        LocalDateTime notifyDate = (value == null || value.isEmpty()) ? null :
                LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        value = valueMap.get("keyType");
        KeyType keyType = (value == null || value.isEmpty()) ? null : KeyType.valueOf(value);
        value = valueMap.get("purposeType");
        PurposeType purposeType = (value == null || value.isEmpty()) ? null : PurposeType.valueOf(value);

        KeyDataDto keyData = keyDataService.generateMasterKey(
                id, alias, desc, expirationDate, password, notifyDate);

        log.debug("generateMasterKey3: -> " + keyData);
        return new ResponseEntity<>(keyData, HttpStatus.OK);
    }

    /**
     * Загрузка мастер-ключа в оперативный доступ.
     * @param id
     * @param password
     * @return
     */
    @PutMapping("/master" + "/{id}/load")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyDataDto> loadMasterKey(@PathVariable("id") UUID id, @RequestBody String password) {
        log.info("loadMasterKey: <- id=" + id);

        KeyDataDto keyData = keyDataService.loadMasterKey(id, password.toCharArray());

        log.debug("loadMasterKey: -> " + keyData);
        return new ResponseEntity<>(keyData, HttpStatus.OK);
    }

    /**
     * Создание ключа шифрования данных
     * @param alias
     * @return
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KeyDataDto> generateDataKe(@RequestBody String alias) {
        log.info("generateDataKe: <- alias=" + alias);

        KeyDataDto keyData = keyDataService.generateDataKey(alias);

        log.debug("generateDataKe: -> " + keyData);
        return new ResponseEntity<>(keyData, HttpStatus.OK);
    }

    /**
     * Обновление атрибутов ключа шифрования данных
     * @param from
     * @return
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateKeyData(@RequestBody UpdateKeyDataRequest from) {
        log.info("updateDataKey: <- key=" + from);

        keyDataService.updateKeyData(from.getId(), from.getKey());

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
