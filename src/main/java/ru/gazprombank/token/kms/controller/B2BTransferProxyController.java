package ru.gazprombank.token.kms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gazprombank.token.kms.entity.b2bDto.CheckPayRequest;
import ru.gazprombank.token.kms.entity.b2bDto.CheckPayResponse;
import ru.gazprombank.token.kms.entity.b2bDto.TransferRequest;
import ru.gazprombank.token.kms.entity.b2bDto.TransferResponse;
import ru.gazprombank.token.kms.service.B2BProxyTransferService;

@Slf4j
@RestController
@RequestMapping("/b2b/")
@RequiredArgsConstructor
public class B2BTransferProxyController {

    private final B2BProxyTransferService b2BProxyTransferService;

    /**
     * Запрос проверки карты и расчета комиссии.
     * @param data
     * @return
     */
    @PostMapping(path = "/check", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CheckPayResponse> check(@RequestBody @Valid CheckPayRequest data) {
        log.info(String.format("check: <- key=" + data));

        CheckPayResponse resp = b2BProxyTransferService.checkPay(data);

        log.debug("check: -> " + resp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Запрос перевода.
     * @param data
     * @return
     */
    @PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponse> transfer(@RequestBody @Valid TransferRequest data) {
        log.info(String.format("transfer: <- key=" + data));

        TransferResponse resp = b2BProxyTransferService.transfer(data);

        log.debug("transfer: -> " + resp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
