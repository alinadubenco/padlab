package edu.dubenco.alina.ms.warehouse.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * This interface is Spring Data Repository that generates in runtime classes for persisting 
 * and retrieving {@link Balance}s from DB.
 * 
 * @author Alina Dubenco
 *
 */
public interface BalanceRepository extends JpaRepository<Balance, Long> {

	@Query(value = "SELECT b.quantity FROM Balance b WHERE b.product = :productId ORDER BY b.date DESC")
	List<Integer> findInitialProductQuantity(@Param("productId") long productId);
}