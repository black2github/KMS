package ru.gpb.kms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KmsApplication.class, args);
	}

}
