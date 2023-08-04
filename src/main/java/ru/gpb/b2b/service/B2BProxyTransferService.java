package ru.gpb.b2b.service;

import ru.gpb.b2b.Dto.CheckPayResponse;
import ru.gpb.b2b.Dto.TransferRequest;
import ru.gpb.b2b.Dto.TransferResponse;
import ru.gpb.b2b.Dto.CheckPayRequest;

/**
 * Проверка и формирование перевода.
 */
public interface B2BProxyTransferService {
    CheckPayResponse checkPay(CheckPayRequest request);

    TransferResponse transfer(TransferRequest request);
}
