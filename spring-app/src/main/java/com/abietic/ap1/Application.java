package com.abietic.ap1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;
// import org.springframework.web.reactive.config.CorsRegistry;
// import org.springframework.web.reactive.config.WebFluxConfigurer;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// @Bean
	// public WebFluxConfigurer corsConfigurer() {
	// 	return new WebFluxConfigurer() {

	// 		@Override
	// 		public void addCorsMappings(CorsRegistry registry) {
	// 			WebFluxConfigurer.super.addCorsMappings(registry);
	// 			registry.addMapping("/another_greeting").allowedOrigins("*");
	// 		}
			
	// 	};
	// }

}
