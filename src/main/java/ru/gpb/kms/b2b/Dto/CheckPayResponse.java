package ru.gpb.kms.b2b.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckPayResponse {
    private String responseCode;
    private String responseMessage;
    private String terminal;
    private Amount amount;
    private Comission comission;
    private Amount conversion;
    private Recipient maskFio;
}
