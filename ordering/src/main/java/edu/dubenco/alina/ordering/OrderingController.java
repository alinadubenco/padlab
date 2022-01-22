package edu.dubenco.alina.ordering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.dubenco.alina.ordering.repo.Order;
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
		return orderRepository.save(order);
	}
	
}
