package ru.gpb.kms.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RotateKeyScheduler {

    private final KeyDataService keyDataService;

    // Метод, который будет выполняться периодически
    @Scheduled(fixedDelay = 60000) // Задержка в 1 минуту между выполнениями
    public void rotateDataKeys() {
        keyDataService.rotateDataKey();
    }

}
