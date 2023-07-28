package ru.gazprombank.token.kms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.form.FormData;
import jakarta.validation.Valid;
import org.hibernate.annotations.MapKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.gazprombank.token.kms.entity.Dto.KeyDataDto;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.entity.KeyType;
import ru.gazprombank.token.kms.entity.PurposeType;
import ru.gazprombank.token.kms.service.ApplicationException;
import ru.gazprombank.token.kms.service.KeyDataService;
import ru.gazprombank.token.kms.service.KeyNotFoundApplicationException;
import ru.gazprombank.token.kms.service.SecurityApplicationException;
import ru.gazprombank.token.kms.util.mapper.KeyDataMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/keys/")
public class KeyDataController {

    private static final Logger log = LoggerFactory.getLogger(KeyDataController.class);

    private final KeyDataService keyDataService;

    private final KeyDataMapper keyDataMapper;

    @Autowired
    public KeyDataController(KeyDataService keyDataService, KeyDataMapper keyDataMapper) {
        this.keyDataService = keyDataService;
        this.keyDataMapper = keyDataMapper;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KeyData>> list() {
        log.info("list: <- ");
        return new ResponseEntity<>(keyDataService.listAll(), HttpStatus.OK);
    }

    @PostMapping(path = "/master", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyData> generateMasterKey(@RequestBody KeyData formData) {
        log.info(String.format("generateMasterKey: <- " + formData));
        try {
            KeyData keyData = keyDataService.generateMasterKey(formData.getId(), formData.getAlias(), formData.getDescription(), formData.getExpiryDate(),
                    "123".toCharArray(), formData.getNotifyDate());

            log.debug("generateMasterKey: -> " + keyData);
            return new ResponseEntity<>(keyData, HttpStatus.OK);
        } catch (Exception ex) {
            riseError("generateMasterKey", ex);
        }
        // We shouldn't be here
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/master4", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyDataDto> generateMasterKey4(@RequestBody MasterKeyGenerationRequest formData) {
        log.info(String.format("generateMasterKey4: <-. "));
        KeyDataDto key = formData.getKeyDataDto();
        char[] password = (formData.getPassword() == null || formData.getPassword().isEmpty())
                ? null : formData.getPassword().toCharArray();
        log.info(String.format("generateMasterKey4: key=" + key));
        try {
            UUID id = (key.getId() == null) ? null : UUID.fromString(key.getId());
            // генерация мастер-ключа
            KeyData keyData = keyDataService.generateMasterKey(id, key.getAlias(),
                    key.getDescription(), key.getExpiryDate(), password, key.getNotifyDate());

            KeyDataDto dto = keyDataMapper.toDto(keyData);
            log.debug("generateMasterKey4: -> " + keyData);

            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception ex) {
            riseError("generateMasterKey4", ex);
        }
        // We shouldn't be here
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // рабочий вариант для формы
    @PostMapping(path = "/master3", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyData> generateMasterKey3(@RequestBody MultiValueMap<String, String> formData) {
        log.info(String.format("generateMasterKey3: <- " + formData.toSingleValueMap()));
        try {
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

            KeyData keyData = keyDataService.generateMasterKey(
                    id, alias, desc, expirationDate, password, notifyDate);

            log.debug("generateMasterKey3: -> " + keyData);
            return new ResponseEntity<>(keyData, HttpStatus.OK);
        } catch (Exception ex) {
            riseError("generateMasterKey3", ex);
        }
        // We shouldn't be here
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/master2", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<KeyData> generateMasterKey2(@RequestBody KeyData formData) {
        log.info(String.format("generateMasterKey2: <- " + formData));

        // public ResponseEntity<KeyData> generateMasterKey(@RequestBody /*@Valid*/ UUID id,
        //                                                  @RequestBody  String alias,
        //                                                  @RequestBody  String description,
        //                                                  @RequestBody  LocalDateTime expirationDate,
        //                                                  @RequestBody  String password,
        //                                                  @RequestBody  LocalDateTime notifyDate,
        //                                                  @RequestBody KeyType keyType,
        //                                                  @RequestBody PurposeType purposeType,
        //                                                  BindingResult bindingResult) {
        // public ResponseEntity<KeyData> generateMasterKey(@RequestBody KeyData key, BindingResult bindingResult ) {
        //     log.info(String.format("generateMasterKey: <- ." + key));
        //     log.info(String.format("generateMasterKey: <- id=%s, alias=%s, desc=%s, expDate=%s, notifyDate=%s, password=%s",
        //             id, alias, description, expirationDate, notifyDate, password));
        try {
            // if (bindingResult.hasErrors()) {
            //     List<ObjectError> errs = bindingResult.getAllErrors();
            //     for (ObjectError error : errs) {
            //         if (!"password".equals(error.getCode())) {
            //             throw new InvalidArgumentApplicationException(errs.get(0).getDefaultMessage());
            //         }
            //     }
            // }
            // String value = valueMap.get("id");
            // UUID id = (formData.getId()  == null || value.isEmpty()) ? null : UUID.fromString(value);
            // value = valueMap.get("password");
            // char[] password = (value == null) ? null : value.toCharArray();
            // value = valueMap.get("alias");
            // String alias = (value == null || value.isEmpty()) ? null : value;
            // value = valueMap.get("description");
            // String desc = (value == null || value.isEmpty()) ? null : value;
            // value = valueMap.get("expirationDate");
            // LocalDateTime expirationDate = (value == null || value.isEmpty()) ? null :
            //         LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); //2028-07-26T19:40:22.7440662
            // value = valueMap.get("algorithm");
            // String algorithm = (value == null || value.isEmpty()) ? null : value;
            // value = valueMap.get("notifyDate");
            // LocalDateTime notifyDate = (value == null || value.isEmpty()) ? null :
            //         LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            // value = valueMap.get("keyType");
            // KeyType keyType = (value == null || value.isEmpty() ) ? null : KeyType.valueOf(value);
            // value = valueMap.get("purposeType");
            // PurposeType purposeType = (value == null || value.isEmpty() ) ? null : PurposeType.valueOf(value);

            KeyData keyData = keyDataService.generateMasterKey(formData.getId(), formData.getAlias(), formData.getKey(),
                    formData.getExpiryDate(), "123".toCharArray(), formData.getNotifyDate());

            log.debug("generateMasterKey2: -> " + keyData);
            return new ResponseEntity<>(keyData, HttpStatus.OK);
        } catch (Exception ex) {
            riseError("generateMasterKey2", ex);
        }
        // We shouldn't be here
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /*
     * Формирование ResponseStatusException для передачи клиенту расширенной информации об ошибке.
     * Требует установки в application.properties настройки server.error.include-message=always
     */
    private void riseError(String methodName, Exception e) {
        log.warn(methodName + ": error -> " + e.getMessage());
        System.out.println(": error -> " + e.getMessage());
        if (e instanceof SecurityApplicationException) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } else if (e instanceof KeyNotFoundApplicationException) {
            // throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } else if (e instanceof DataIntegrityViolationException
                || e instanceof ApplicationException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } else if (e instanceof JsonProcessingException) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
