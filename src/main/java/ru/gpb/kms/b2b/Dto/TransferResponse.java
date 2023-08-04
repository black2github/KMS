package ru.gpb.kms.b2b.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String responseCode;
    private String responseMessage;
    private UUID transactionID;
    private String status;
    private ZonedDateTime realOperDate;
    private String utrnno;
}
