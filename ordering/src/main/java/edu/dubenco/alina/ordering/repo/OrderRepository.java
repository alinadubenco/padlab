package edu.dubenco.alina.ordering.repo;
import org.springframework.data.repository.CrudRepository;

/**
 * This interface is Spring Data Repository that generates in runtime classes for persisting 
 * and retrieving {@link Order}s from DB.
 * 
 * @author Alina Dubenco
 *
 */
public interface OrderRepository extends CrudRepository<Order, String> {

}
