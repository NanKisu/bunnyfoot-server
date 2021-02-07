package kr.co.bunnyfoot.bunnyfoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableFeignClients
public class BunnyfootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BunnyfootApplication.class, args);
	}

}