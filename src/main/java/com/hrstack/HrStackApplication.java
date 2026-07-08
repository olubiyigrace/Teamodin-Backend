package com.hrstack;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class HrStackApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("MAIL_PORT", dotenv.get("MAIL_PORT"));
		System.setProperty("MAIL_HOST", dotenv.get("MAIL_HOST"));
		System.setProperty("SUPPORT_EMAIL", dotenv.get("SUPPORT_EMAIL"));
		System.setProperty("APP_PASSWORD", dotenv.get("APP_PASSWORD"));
		System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));
		System.setProperty("JWT_ACCESS_TOKEN_EXPIRATION", dotenv.get("JWT_ACCESS_TOKEN_EXPIRATION"));
		System.setProperty("JWT_REFRESH_TOKEN_EXPIRATION", dotenv.get("JWT_REFRESH_TOKEN_EXPIRATION"));
		System.setProperty("CLOUD_NAME", dotenv.get("CLOUD_NAME"));
		System.setProperty("API_KEY", dotenv.get("API_KEY"));
		System.setProperty("API_SECRET", dotenv.get("API_SECRET"));
		SpringApplication.run(HrStackApplication.class, args);
	}

}
