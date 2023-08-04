package ru.gpb.b2b.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String key;
    private String systemID;
    private String channel;
    private String orderID;
    private String requestID;
    private String traceID;
    private String type;
    private String terminal;
    private boolean excludeTFile;
    private Source source;
    private Destination destination;
    private String recipient;
    private Amount amount;
    private Comission comission;
    private Amount conversion;
    private String mcc;
    private String changeReasonCode;
    private boolean absBalanceChanged;
    private String recipientInn;
    private String payerInn;
    private String reason;
    private String fullReason;
    private boolean recipientIsResident;
    private boolean payerIsResident;
}
