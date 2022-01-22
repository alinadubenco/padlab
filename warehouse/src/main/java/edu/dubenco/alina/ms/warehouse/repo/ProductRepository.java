package edu.dubenco.alina.ms.warehouse.repo;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * This interface is Spring Data Repository that generates in runtime classes for persisting 
 * and retrieving {@link Product}s from DB.
 * 
 * @author Alina Dubenco
 *
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	@Query(value = "SELECT p FROM Product p WHERE p.category=:category AND (p.name LIKE %:txt% OR p.description LIKE %:txt%) AND p.price >= :minPrice AND p.price <= :maxPrice")
	List<Product> find(@Param("txt") String txt, @Param("category") String category, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}