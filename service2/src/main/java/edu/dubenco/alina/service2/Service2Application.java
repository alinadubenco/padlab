package edu.dubenco.alina.service2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Service2Application {
	private static Logger LOG = LoggerFactory.getLogger(Service2Application.class);

	@Value("${service.registration.uri}")
	String registrationUri;
	
	@Value("${service.url}")
	String serviceUrl;

	
	public static void main(String[] args) {
		SpringApplication.run(Service2Application.class, args);
	}


	@EventListener(ApplicationReadyEvent.class)
	public void registerServiceInGateway() {
		HttpClient httpClient = HttpClient.newBuilder()
		        .version(HttpClient.Version.HTTP_1_1)
		        .connectTimeout(Duration.ofSeconds(3))
		        .executor(Executors.newFixedThreadPool(1))
		        .build();
		
		String body = "service2" + "\n" + serviceUrl;
		
		HttpRequest registrationRequest = HttpRequest.newBuilder()
				.uri(URI.create(registrationUri))
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		
		try {
			HttpResponse<String> response = httpClient.send(registrationRequest, HttpResponse.BodyHandlers.ofString());
			if(response.statusCode() == 200) {
				LOG.info("Successfully registered in Gateway");
			} else {
				LOG.error("There was an issue during service registration to Gateway. Gateway responded with status=" + response.statusCode() + " and message='" + response.body() + "'");
			}
		} catch (IOException e) {
			LOG.error("Failed to register service to Gateway.", e);
		} catch (InterruptedException e) {
			LOG.warn("Service registration has been interrupted");
			Thread.currentThread().interrupt();
		}
		
	}
}
