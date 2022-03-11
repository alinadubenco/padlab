package edu.dubenco.alina.service1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Alina Dubenco
 *
 */
@RestController
public class Service1Controller {
	private static final Logger LOG = LoggerFactory.getLogger(Service1Controller.class);

	@GetMapping("/service1")
	public String get() {
		LOG.info("Service 1");
		return "Response from Service1";
	}
}
