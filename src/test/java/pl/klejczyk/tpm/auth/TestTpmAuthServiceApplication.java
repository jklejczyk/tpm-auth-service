package pl.klejczyk.tpm.auth;

import org.springframework.boot.SpringApplication;

public class TestTpmAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TpmAuthServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
