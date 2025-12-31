package com.project.medinova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedinovaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedinovaApplication.class, args);
	}

}
