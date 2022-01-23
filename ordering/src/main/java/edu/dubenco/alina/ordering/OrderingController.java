package edu.dubenco.alina.ordering;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import edu.dubenco.alina.ordering.repo.Order;
import edu.dubenco.alina.ordering.repo.OrderDetail;
import edu.dubenco.alina.ordering.repo.OrderRepository;

/**
 * This class handles the REST APIs for the Ordering microservice
 *  
 * @author Alina Dubenco
 *
 */
@RestController
public class OrderingController {
	public static final String ORDERING = "ordering";
	private static final String ORDERING_ORDERS = "/" + ORDERING + "/orders";
	
	@Value("${gateway.url}")
	private String gatewayUrl;

	private RestTemplate restTemplate = new RestTemplate();
	
	@Autowired
	private OrderRepository orderRepository;
	
	@GetMapping(ORDERING_ORDERS)
	public Iterable<Order> searchOrders() {
		return orderRepository.findAll();
	}

	@GetMapping(ORDERING_ORDERS + "/{id}")
	public Order getOrder(@PathVariable String id) {
		return orderRepository.findById(id).orElseThrow();
	}
	
	@PostMapping(ORDERING_ORDERS)
	public Order placeOrder(@RequestBody Order order) {
		
		String reservationId = reserveProductsInWarehouse(order);
		
		try {
			order = orderRepository.save(order);
		} catch (Exception e) {
			cancelReservedProducts(reservationId);
			throw e;
		}
		
		confirmReservedProducts(reservationId);
		
		return order;
	}

	private String reserveProductsInWarehouse(Order order) {
		HttpEntity<DocumentInfo> docHttpEntity = orderToHttpEntity(order);
		ResponseEntity<String> response = restTemplate.postForEntity(gatewayUrl + "/warehouse/reservation/", docHttpEntity, String.class);
		if(response.getStatusCode() == HttpStatus.OK) {
			return response.getBody();
		} else {
			throw new RestClientException("Failed to reserve the products: " + response.getBody());
		}
	}

	private void confirmReservedProducts(String reservationId) {
		restTemplate.put(gatewayUrl + "/warehouse/reservation/" + reservationId + "/confirm", "");
	}

	private void cancelReservedProducts(String reservationId) {
		restTemplate.put(gatewayUrl + "/warehouse/reservation/" + reservationId + "/cancel", "");
	}
	
	private HttpEntity<DocumentInfo> orderToHttpEntity(Order order) {
		DocumentInfo doc = new DocumentInfo();
		doc.setNumber(order.getId());
		doc.setDate(new Date());
		List<InputOutputInfo> details = new ArrayList<>();
		for(OrderDetail od : order.getDetails()) {
			details.add(new InputOutputInfo(od.getProductId(), od.getQuantity()));
		}
		doc.setInputOutputs(details);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<DocumentInfo> docHttpEntity = new HttpEntity<>(doc, headers);
		return docHttpEntity;
	}
	
}
