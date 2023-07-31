package ru.gazprombank.token.kms.entity.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class ErrorResponseDto {
    private ZonedDateTime timestamp;
    private String message;
    private int code;

    public ErrorResponseDto(String message) {
        this.message = message;
        this.timestamp = ZonedDateTime.now();
    }

    public ErrorResponseDto(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = ZonedDateTime.now();
    }
}
