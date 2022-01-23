package edu.dubenco.alina.ms.warehouse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.dubenco.alina.ms.warehouse.repo.BalanceRepository;
import edu.dubenco.alina.ms.warehouse.repo.Document;
import edu.dubenco.alina.ms.warehouse.repo.DocumentRepository;
import edu.dubenco.alina.ms.warehouse.repo.InputOutput;
import edu.dubenco.alina.ms.warehouse.repo.InputOutputRepository;
import edu.dubenco.alina.ms.warehouse.repo.Product;
import edu.dubenco.alina.ms.warehouse.repo.ProductRepository;

/**
 * This class handles all the REST APIs for the Warehouse microservice.
 * 
 * @author Alina Dubenco
 *
 */
@RestController
public class WarehouseController {

	public static final String WAREHOUSE = "warehouse";
	private static final String WAREHOUSE_PRODUCTS = "/" + WAREHOUSE + "/products";
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private DocumentRepository docRepository;
	
	@Autowired
	private InputOutputRepository ioRepository;

	@Autowired
	private BalanceRepository balanceRepository;
	
	@GetMapping(WAREHOUSE_PRODUCTS)
	public List<Product> searchProducts(
			@RequestParam(name="text") String txt, 
			@RequestParam(name="category") String category, 
			@RequestParam(name="minPrice", defaultValue = "0") BigDecimal minPrice, 
			@RequestParam(name="maxPrice", defaultValue = "999999999") BigDecimal maxPrice) {
		
		return productRepository.find(txt, category, minPrice, maxPrice);
	}

	@GetMapping(WAREHOUSE_PRODUCTS + "/{id}")
	public Product getProduct(@PathVariable Long id) {
		Product prod = productRepository.findById(id).orElseThrow();
		int availableQuantity = getAvailableProductQuantity(id);
		prod.setQuantity(availableQuantity);
		return prod;
	}

	@PostMapping(WAREHOUSE_PRODUCTS)
	public Product addProduct(@RequestBody Product product) {
		return productRepository.save(product);
	}

	@PutMapping(WAREHOUSE_PRODUCTS)
	public Product updateProduct(@RequestBody Product product) {
		return productRepository.save(product);
	}
	
	@PostMapping("/" + WAREHOUSE + "/supply")
	public void supplyProduct(@RequestBody DocumentInfo supplyInfo) {
		Document doc = new Document(supplyInfo.getNumber(), supplyInfo.getDate(), true);
		doc = docRepository.save(doc);
		for(InputOutputInfo r : supplyInfo.getInputOutputs()) {
			InputOutput io = new InputOutput(doc, r.getProduct(), r.getQuantity());
			ioRepository.save(io);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PostMapping("/" + WAREHOUSE + "/reservation")
	public String reserveProducts(@RequestBody DocumentInfo reservationInfo) {
		Document doc = new Document(reservationInfo.getNumber(), reservationInfo.getDate(), false);
		doc = docRepository.save(doc);
		for(InputOutputInfo r : reservationInfo.getInputOutputs()) {
			
			int availableQuantity = getAvailableProductQuantity(r.getProduct());
			if(availableQuantity < r.getQuantity()) {
				throw new RuntimeException("Not enough quantity for product " + r.getProduct());
			}
			
			InputOutput io = new InputOutput(doc, r.getProduct(), -1 * r.getQuantity());
			ioRepository.save(io);
		}
		return doc.getId().toString();
	}

	@PutMapping("/" + WAREHOUSE + "/reservation/{reservationId}/confirm")
	public void confirmReservation(@PathVariable long reservationId) {
		Optional<Document> reservation = docRepository.findById(reservationId);
		if(reservation.isPresent()) {
			reservation.get().setConfirmed(true);
			docRepository.save(reservation.get());
		}
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PutMapping("/" + WAREHOUSE + "/reservation/{reservationId}/cancel")
	public void cancelReservation(@PathVariable long reservationId) {
		Optional<Document> docOpt = docRepository.findById(reservationId);
		if(docOpt.isPresent()) {
			Document doc = docOpt.get();
			for(InputOutput io : doc.getInputOutputs()) {
				ioRepository.deleteById(io.getId());
			}
			docRepository.deleteById(doc.getId());
		}
	}
	
	private int getAvailableProductQuantity(long productId) {
		List<Integer> quantities = balanceRepository.findInitialProductQuantity(productId);
		int iniQuantity = 0;
		if(quantities != null && !quantities.isEmpty()) {
			iniQuantity = quantities.get(0);
		}
		int ioSum = ioRepository.findProductInputOutputSum(productId);
		int quanttity = iniQuantity + ioSum;
		if(quanttity < 0) {
			quanttity = 0;
		}
		return quanttity;
	}
}
