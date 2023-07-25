package ru.gazprombank.token.kms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.gazprombank.token.kms.entity.KeyData;
import ru.gazprombank.token.kms.service.ApplicationException;
import ru.gazprombank.token.kms.service.KeyDataService;
import ru.gazprombank.token.kms.service.KeyNotFoundApplicationException;
import ru.gazprombank.token.kms.service.SecurityApplicationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/keys/")
public class KeyDataController {

    private static final Logger log = LoggerFactory.getLogger(KeyDataController.class);

    private final KeyDataService keyDataService;

    @Autowired
    public KeyDataController(KeyDataService keyDataService) {
        this.keyDataService = keyDataService;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KeyData>> list() {
        log.info("list: <- ");
        return new ResponseEntity<>(keyDataService.listAll(), HttpStatus.OK);
    }

    @PostMapping()
    @PreAuthorize("hasRole('MASTER')")
    // public ResponseEntity<KeyData> generateMasterKey(@RequestBody /*@Valid*/ UUID id,
    //                                                  @RequestBody  String alias,
    //                                                  @RequestBody  String desc,
    //                                                  @RequestBody  LocalDateTime expirationDate,
    //                                                  char[] password,
    //                                                  @RequestBody  LocalDateTime notifyDate,
    //                                                  BindingResult bindingResult) {
    public ResponseEntity<KeyData> generateMasterKey(@RequestBody KeyData key, BindingResult bindingResult ) {
        log.info(String.format("generateMasterKey: <- ." + key));
        // log.info(String.format("generateMasterKey: <- id=%s, alias=%s, desc=%s, expDate=%s, notifyDate=%s",
        //         id, alias, desc, expirationDate, notifyDate));
        try {
            // if (bindingResult.hasErrors()) {
            //     List<ObjectError> errs = bindingResult.getAllErrors();
            //     for (ObjectError error : errs) {
            //         if (!"password".equals(error.getCode())) {
            //             throw new InvalidArgumentApplicationException(errs.get(0).getDefaultMessage());
            //         }
            //     }
            // }
            // KeyData keyData = keyDataService.generateMasterKey(id, alias, desc, expirationDate, password, notifyDate);
            KeyData keyData = keyDataService.generateMasterKey(null, null, null, null, null, null);
            log.debug("generateMasterKey: -> " + keyData);
            return new ResponseEntity<>(keyData, HttpStatus.OK);
        } catch (Exception ex) {
            riseError("generateMasterKey", ex);
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
