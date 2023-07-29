package ru.gazprombank.token.kms.entity.Dto;

public class ErrorResponseDto {
    private String message;
    private int code;

    public ErrorResponseDto(String message) {
        this.message = message;
    }

    public ErrorResponseDto(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
