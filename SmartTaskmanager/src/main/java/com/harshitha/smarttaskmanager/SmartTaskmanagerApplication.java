package com.harshitha.smarttaskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartTaskmanagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartTaskmanagerApplication.class, args);
	}
}