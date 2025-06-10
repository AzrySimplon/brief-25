package fr.azry.TPSpringBoot.repository;

import fr.azry.TPSpringBoot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}