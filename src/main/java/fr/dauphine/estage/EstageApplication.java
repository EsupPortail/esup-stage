package fr.dauphine.estage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class EstageApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EstageApplication.class, args);
	}

}
