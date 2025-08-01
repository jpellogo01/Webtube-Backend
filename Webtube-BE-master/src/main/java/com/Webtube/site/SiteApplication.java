package com.Webtube.site;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SiteApplication {

	@Value("${openai.api.key}")
	private String openAiApiKey;
	public static void main(String[] args) {
		SpringApplication.run(SiteApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add("Authorization", "Bearer " + openAiApiKey);
			return execution.execute(request, body);
		});

		return restTemplate;
	}
}
