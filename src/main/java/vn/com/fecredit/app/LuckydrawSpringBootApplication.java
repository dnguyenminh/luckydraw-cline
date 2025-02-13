package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LuckydrawSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuckydrawSpringBootApplication.class, args);
	}

}