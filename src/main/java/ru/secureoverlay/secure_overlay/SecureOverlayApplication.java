package ru.secureoverlay.secure_overlay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.secureoverlay.secure_overlay.config.OverlayProperties;

@SpringBootApplication
@EnableConfigurationProperties(
		OverlayProperties.class
)
public class SecureOverlayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureOverlayApplication.class, args);
	}

}
