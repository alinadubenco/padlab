package edu.dubenco.alina.ordering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.dubenco.alina.ordering.repo.Order;
import edu.dubenco.alina.ordering.repo.OrderRepository;

@RestController
public class OrderingController {
	private static final String ORDERING_ORDERS = "/ordering/orders";

	@Autowired
	private OrderRepository orderRepository;
	
	@GetMapping(ORDERING_ORDERS)
	public Iterable<Order> searchProducts() {
		return orderRepository.findAll();
	}

	@GetMapping(ORDERING_ORDERS + "/{id}")
	public Order getProduct(@PathVariable String id) {
		return orderRepository.findById(id).orElseThrow();
	}
	
	@PostMapping(ORDERING_ORDERS)
	public Order placeOrder(@RequestBody Order order) {
		return orderRepository.save(order);
	}
	
}
