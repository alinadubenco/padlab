package edu.dubenco.alina.ms.warehouse.repo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This interface is Spring Data Repository that generates in runtime classes for persisting 
 * and retrieving {@link Document}s from DB.
 * 
 * @author Alina Dubenco
 *
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

}