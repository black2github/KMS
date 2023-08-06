package ru.gpb.kms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gpb.kms.entity.Dto.ChangeKeyStatusRequest;
import ru.gpb.kms.entity.Dto.MasterKeyGenerationRequest;
import ru.gpb.kms.entity.Dto.UpdateKeyDataRequest;
import ru.gpb.kms.entity.Dto.KeyDataDto;
import ru.gpb.kms.service.KeyDataService;

import java.util.List;
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
     * Отметка об удалении на ключе.
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@RequestBody String id) {
    // public ResponseEntity<Void> delete(@RequestBody @Valid UUID id) {
        log.info("delete: <- id='" + id + "'");
        keyDataService.delete(id.toString());
        return new ResponseEntity<>(HttpStatus.OK);
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
        log.info(String.format("generateMasterKey: <- key=" + formData.getKey()));

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
     * Загрузка мастер-ключа в оперативный доступ.
     *
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
     *
     * @param alias
     * @return
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KeyDataDto> generateDataKey(@RequestBody String alias) {
        log.info("generateDataKe: <- alias=" + alias);

        KeyDataDto keyData = keyDataService.generateDataKey(alias);

        log.debug("generateDataKe: -> " + keyData);
        return new ResponseEntity<>(keyData, HttpStatus.OK);
    }

    /**
     * Обновление атрибутов ключа шифрования данных.
     *
     * @param req
     * @return
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateKeyData(@RequestBody UpdateKeyDataRequest req) {
        log.info("updateDataKey: <- key=" + req);
        keyDataService.updateKeyData(req.getId(), req.getKey());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Изменение статуса ключа.
     *
     * @param req
     * @return
     */
    @PostMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(@RequestBody ChangeKeyStatusRequest req) {
        log.info("updateDataKey: <- key=" + req);
        keyDataService.changeStatus(req.getId(), req.getStatus());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
