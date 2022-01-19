package edu.dubenco.alina.ms.warehouse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WarehouseController {

	private static final String WAREHOUSE_PRODUCTS = "/warehouse/products";
	
	@Autowired
	private ProductRepository productRepository;
	
	@GetMapping(WAREHOUSE_PRODUCTS)
	public List<Product> searchProducts() {
		return productRepository.findAll();
	}

	@GetMapping(WAREHOUSE_PRODUCTS + "/{id}")
	public Product getProduct(@PathVariable Long id) {
		return productRepository.findById(id).orElseThrow();
	}
}
