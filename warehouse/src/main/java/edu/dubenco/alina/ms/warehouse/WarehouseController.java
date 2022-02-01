package edu.dubenco.alina.ms.warehouse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import net.sf.hajdbc.sql.Driver;
import net.sf.hajdbc.invocation.InvokeOnManyInvocationStrategy;

/**
 * This class handles all the REST APIs for the Warehouse microservice.
 * 
 * @author Alina Dubenco
 *
 */
@RestController
public class WarehouseController {
	
	private static final Logger LOG = LoggerFactory.getLogger(WarehouseController.class);

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
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@GetMapping(WAREHOUSE_PRODUCTS)
	public List<Product> searchProducts(
			@RequestParam(name="text") String txt, 
			@RequestParam(name="category") String category, 
			@RequestParam(name="minPrice", defaultValue = "0") BigDecimal minPrice, 
			@RequestParam(name="maxPrice", defaultValue = "999999999") BigDecimal maxPrice) {
		
		LOG.debug("Searching products");
		return productRepository.find(txt, category, minPrice, maxPrice);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@GetMapping(WAREHOUSE_PRODUCTS + "/{id}")
	public Product getProduct(@PathVariable Long id) {
		LOG.debug("Getting product details");
		Product prod = productRepository.findById(id).orElseThrow();
		int availableQuantity = getAvailableProductQuantity(id);
		prod.setQuantity(availableQuantity);
		return prod;
	}
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PostMapping(WAREHOUSE_PRODUCTS)
	public Product addProduct(@RequestBody Product product) {
		LOG.debug("Additng product");
		return productRepository.save(product);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PutMapping(WAREHOUSE_PRODUCTS)
	public Product updateProduct(@RequestBody Product product) {
		LOG.debug("Updating product");
		return productRepository.save(product);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PostMapping("/" + WAREHOUSE + "/supply")
	public void supplyProduct(@RequestBody DocumentInfo supplyInfo) {
		LOG.debug("Supplying products");
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
		LOG.debug("Reserving products");
		Document doc = new Document(reservationInfo.getNumber(), reservationInfo.getDate(), false);
		doc = docRepository.save(doc);
		for(InputOutputInfo r : reservationInfo.getInputOutputs()) {
			
			int availableQuantity = getAvailableProductQuantity(r.getProduct());
			if(availableQuantity < r.getQuantity()) {
				LOG.warn("\"Not enough quantity for product \" + r.getProduct()");
				throw new RuntimeException("Not enough quantity for product " + r.getProduct());
			}
			
			InputOutput io = new InputOutput(doc, r.getProduct(), -1 * r.getQuantity());
			ioRepository.save(io);
		}
		return doc.getId().toString();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PutMapping("/" + WAREHOUSE + "/reservation/{reservationId}/confirm")
	public void confirmReservation(@PathVariable long reservationId) {
		LOG.debug("Confirming reservation");
		Optional<Document> reservation = docRepository.findById(reservationId);
		if(reservation.isPresent()) {
			reservation.get().setConfirmed(true);
			docRepository.save(reservation.get());
		}
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@PutMapping("/" + WAREHOUSE + "/reservation/{reservationId}/cancel")
	public void cancelReservation(@PathVariable long reservationId) {
		LOG.debug("Cancelling reservation");
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
		int quanttity = 0;
		try {
			int iniQuantity = 0;
			List<Integer> quantities = balanceRepository.findInitialProductQuantity(productId);
			if(quantities != null && !quantities.isEmpty()) {
				iniQuantity = quantities.get(0);
			}
			int ioSum = ioRepository.findProductInputOutputSum(productId);
			quanttity = iniQuantity + ioSum;
			if(quanttity < 0) {
				quanttity = 0;
			}
		} catch(Exception e) {
			LOG.error("Failed to get available product quantity. Will use 0.");
		}
		return quanttity;
	}
}
