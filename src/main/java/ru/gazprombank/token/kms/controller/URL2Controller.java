package ru.gazprombank.token.kms.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;

@RestController
public class URL2Controller {
    private static final Logger log = LoggerFactory.getLogger(URL2Controller.class);

    private String targetUrl = "http://localhost:8888/url3"; // URL, на который будет выполнено перенаправление

    @GetMapping("/url2")
    public void handle(HttpServletResponse response) throws IOException {
        log.info("handle: <- . ");

        // добавляем номер заявки к параметрам
        response.addCookie(new Cookie("X-orderID", URLEncoder.encode("2421-6565775-64747", "UTF-8")));

        // Выполнение перенаправления на указанный URL-адрес
        response.addCookie(new Cookie("X-arg2", "Kabanenko"));
        response.sendRedirect(targetUrl);
    }
}