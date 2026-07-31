package pl.klejczyk.tpm.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TpmAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TpmAuthServiceApplication.class, args);
	}

}
