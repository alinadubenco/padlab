package edu.dubenco.alina.ms.warehouse.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * This interface is Spring Data Repository that generates in runtime classes for persisting 
 * and retrieving {@link InputOutput}s from DB.
 * 
 * @author Alina Dubenco
 *
 */
public interface InputOutputRepository extends JpaRepository<InputOutput, Long> {

	@Query(value = "SELECT SUM(io.quantity) FROM InputOutput io WHERE io.product = :productId")
	Integer findProductInputOutputSum(@Param("productId") long productId);
	
}