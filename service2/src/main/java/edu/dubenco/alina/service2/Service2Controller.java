package edu.dubenco.alina.service2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Alina Dubenco
 *
 */
@RestController
public class Service2Controller {
	private static final Logger LOG = LoggerFactory.getLogger(Service2Controller.class);

	@GetMapping("/service2")
	public String get() {
		LOG.info("Service 2");
		return "Response from service2";
	}
}
