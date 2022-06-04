package com.gaspar.logprocessor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class LogProcessorApplication {

	static {
		System.setProperty("java.awt.headless", "false");
		log.info("System is headless: {}", java.awt.GraphicsEnvironment.isHeadless());
	}

	public static void main(String[] args) {
		SpringApplication.run(LogProcessorApplication.class, args);
	}

}
