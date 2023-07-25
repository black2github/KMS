package ru.gazprombank.token.kms.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

@RestController
public class URL1Controller {
    private static final Logger log = LoggerFactory.getLogger(URL1Controller.class);

    @PostMapping("/url1")
    public void handle(@RequestBody EcoFrontRequest req, HttpServletResponse response) throws IOException {
        HashMap<String, String> data = new HashMap<>();

        log.info("handle: <- ." + req);

        //
        // обрабатываем запрос и возвращаем результат .....
        //
        data.put("requestID", req.getRequestID());
        data.put("operationID", "12345");
        data.put("sum", "12234567");
        data.put("commission", "345677");
        data.put("currency", "RUB");
        data.put("payeeCardID", "1234-5678-88766");
        data.put("payeeMaskedCardNumber", "1234 56** **** 5678");
        data.put("paymentPurpose", "На деревню дедушке за пирожки с вертикальным взлетом.");
        data.put("payeeOGRN", "897655");
        data.put("isResident", "Y");
        data.put("status", "OK");

        for ( String key: data.keySet()) {
            response.addCookie(new Cookie("X-" + key, URLEncoder.encode(data.get(key), "UTF-8")));
        }

        response.addCookie(new Cookie("X-arg1", "Nikita"));
        // Выполнение перенаправления на полученный в запросе URL-адрес
        response.sendRedirect(req.getResponseUrl());
    }
}