package ru.gazprombank.token.kms.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gazprombank.token.kms.entity.b2bDto.CheckPayRequest;
import ru.gazprombank.token.kms.entity.b2bDto.CheckPayResponse;
import ru.gazprombank.token.kms.entity.b2bDto.TransferRequest;
import ru.gazprombank.token.kms.entity.b2bDto.TransferResponse;
import ru.gazprombank.token.kms.service.B2BProxyTransferService;
import ru.gazprombank.token.kms.service.TokenService;

/**
 * B2B Proxy Transfer service implementation class.
 *
 * @author Alexey Sen (alexey.sen@gmail.com)
 * @since 31.07.2023
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class B2BTransferProxyServiceImpl implements B2BProxyTransferService {

    private final TokenService tokenService;

    /**
     * @param request
     * @return
     */
    @Override
    public CheckPayResponse checkPay(CheckPayRequest request) {
        log.info("checkPay: <- req=" + request);
        // преобразование токена в PAN
        String pan = request.getDestination().getVirtualNum();
        if (pan != null) {
            log.info("checkPay: vPAN=" + pan);
            // преобразовать к оригинальному PAN
            pan = tokenService.token2Secret(pan);
            request.getDestination().setPan(pan);
            request.getDestination().setVirtualNum(null);
        }
        // пробросить запрос далее

        // TODO ...
        CheckPayResponse checkPayResponse = new CheckPayResponse();

        log.info("checkPay: -> resp=" + checkPayResponse);
        return checkPayResponse;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public TransferResponse transfer(TransferRequest request) {
        log.info("transfer: <- req=" + request);
        // преобразование токена в PAN
        String pan = request.getDestination().getVirtualNum();
        if (pan != null) {
            log.info("transfer: vPAN=" + pan);
            // преобразовать к оригинальному PAN
            pan = tokenService.token2Secret(pan);
            request.getDestination().setPan(pan);
            request.getDestination().setVirtualNum(null);
        }
        // пробросить запрос далее

        // TODO ...
        TransferResponse transferResponse = new TransferResponse();

        log.info("transfer: -> resp=" + transferResponse);
        return transferResponse;
    }
}
