package ru.gpb.kms.entity.Dto;

import lombok.Data;

/**
 * Данные, отправляемые со стороны Фронта Эко на страницу ПЦ для безопасного ввода реквизитов перевода
 * и расчета комиссии.
 */
@Data
public class EcoFrontRequest {
    private String requestID; // в случае повтороного запроса может повторять данные из орагинального запроса.
    private String vPAN; // виртуальный номер карты отправителя
    private String responseUrl; // URL, по которому нужно перенаправвить результат ввода реквизитов и расчета коммиссии

    // поля, указанные ниже, предназначены для "возврата" пользователя на страницы Эко, если "нажал" кнопки на странице,
    // не имеющие отношения к ПЦ, но отраженные на текущем макете.
    private String buttonUrl1;
    private String buttonUrl2;
    private String buttonUrl3;
    private String buttonUrl4;
    private String buttonUrl5;

    // Поля, указанные ниже являются опциональными. Заполнеы только в случае повторного запроса
    private String errorMsg; // сообщение об ошибке (например, если она возникла на этапе проверки по стоп-листам)
    private int sum; // сумма, если была введена ранее
    private String payeeCardID; // ID карты получателя, если было введено ранее
    private String paymentPurpose; // назначение платежа, если было введено ранее
}
