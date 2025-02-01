package vn.com.fecredit.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LuckydrawApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuckydrawApplication.class, args);
	}

}
