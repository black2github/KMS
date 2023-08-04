package ru.gpb.kms.b2b.controller;

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
import ru.gpb.kms.b2b.service.B2BProxyTransferService;
import ru.gpb.kms.b2b.Dto.CheckPayRequest;
import ru.gpb.kms.b2b.Dto.CheckPayResponse;
import ru.gpb.kms.b2b.Dto.TransferRequest;
import ru.gpb.kms.b2b.Dto.TransferResponse;

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
