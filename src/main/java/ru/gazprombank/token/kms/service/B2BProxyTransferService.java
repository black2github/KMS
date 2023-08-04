package ru.gazprombank.token.kms.service;

import ru.gazprombank.token.kms.entity.b2bDto.CheckPayRequest;
import ru.gazprombank.token.kms.entity.b2bDto.CheckPayResponse;
import ru.gazprombank.token.kms.entity.b2bDto.TransferRequest;
import ru.gazprombank.token.kms.entity.b2bDto.TransferResponse;

/**
 * Проверка и формирование перевода.
 */
public interface B2BProxyTransferService {
    CheckPayResponse checkPay(CheckPayRequest request);

    TransferResponse transfer(TransferRequest request);
}
