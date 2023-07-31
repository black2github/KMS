package ru.gazprombank.token.kms.entity.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Setter
@Getter
public class ErrorResponseDto {
    // "2023-07-31T20:22:52.573907" ->
    // "2023-07-31T18:49:38.434+00:00"
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
