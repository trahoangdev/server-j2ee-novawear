package com.example.novawear;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NovawearApplication {

	public static void main(String[] args) {
		// Load .env file before Spring context starts
		loadDotenv();
		SpringApplication.run(NovawearApplication.class, args);
	}

	private static void loadDotenv() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			// Set environment variables as system properties for Spring to read
			setProperty("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
			setProperty("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
			setProperty("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));
			setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
			setProperty("JWT_EXPIRATION_MS", dotenv.get("JWT_EXPIRATION_MS"));
			setProperty("DB_URL", dotenv.get("DB_URL"));
			setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
			setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

			System.out.println("[Dotenv] Loaded .env file successfully");
		} catch (Exception e) {
			System.out.println("[Dotenv] No .env file found, using default config values");
		}
	}

	private static void setProperty(String key, String value) {
		if (value != null && !value.isBlank() && System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}
}
