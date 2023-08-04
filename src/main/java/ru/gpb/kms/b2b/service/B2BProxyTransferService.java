package ru.gpb.kms.b2b.service;

import ru.gpb.kms.b2b.Dto.CheckPayRequest;
import ru.gpb.kms.b2b.Dto.CheckPayResponse;
import ru.gpb.kms.b2b.Dto.TransferRequest;
import ru.gpb.kms.b2b.Dto.TransferResponse;

/**
 * Проверка и формирование перевода.
 */
public interface B2BProxyTransferService {
    CheckPayResponse checkPay(CheckPayRequest request);

    TransferResponse transfer(TransferRequest request);
}
