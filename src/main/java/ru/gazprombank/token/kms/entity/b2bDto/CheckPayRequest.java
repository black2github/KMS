package ru.gazprombank.token.kms.entity.b2bDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CheckPayRequest {
    private String SystemID;
    private String channel;
    private String orderID;
    private String requestID;
    private String traceID;
    private String type;
    private String terminal;
    private Source source;
    private Destination destination;
    private String recipient;
    private String mcc;
    private String recipientInn;
    private String payerInn;
    private String reason;
    private String fullReason;
    private boolean recipientIsResident;
    private boolean payerIsResident;
}
