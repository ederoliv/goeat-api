package br.com.ederoliv.goeat_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GoeatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoeatApiApplication.class, args);
	}

}
