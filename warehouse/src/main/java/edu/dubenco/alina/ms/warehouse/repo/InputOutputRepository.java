package edu.dubenco.alina.ms.warehouse.repo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This interface is Spring Data Repository that generates in runtime classes for persisting 
 * and retrieving {@link InputOutput}s from DB.
 * 
 * @author Alina Dubenco
 *
 */
public interface InputOutputRepository extends JpaRepository<InputOutput, Long> {

}