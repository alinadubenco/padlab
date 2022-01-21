package edu.dubenco.alina.ms.warehouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.dubenco.alina.ms.warehouse.repo.Product;
import edu.dubenco.alina.ms.warehouse.repo.ProductRepository;

@Configuration
class LoadDatabase {

  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  @Bean
  CommandLineRunner initDatabase(ProductRepository repository) {

    return args -> {
      log.info("Preloading " + repository.save(new Product("Product1", "Description of Prod1")));
      log.info("Preloading " + repository.save(new Product("Product2", "Description of Prod2")));
    };
  }
}