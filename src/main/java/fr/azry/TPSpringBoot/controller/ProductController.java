package fr.azry.TPSpringBoot.controller;

import fr.azry.TPSpringBoot.model.Product;
import fr.azry.TPSpringBoot.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository repository;
    
    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with ID " + id + " not found"
                ));
    }
    
    @PostMapping
    public Product create(@RequestBody Product product) {
        return repository.save(product);
    }
    
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = getById(id); // Using the improved getById method
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        return repository.save(existing);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // Verify the product exists before deleting
        getById(id); // Will throw if not found
        repository.deleteById(id);
    }
    
    @PostMapping("/{id}/duplicate")
    public Product duplicate(@PathVariable Long id) {
        Product existing_product = getById(id); // Using the improved getById method
        Product new_product = new Product();
        new_product.setName(existing_product.getName() + " copy");
        new_product.setPrice(existing_product.getPrice());
        return create(new_product);
    }

    @PostMapping("/bundle")
    public Product createBundle(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bundle must contain at least one product"
            );
        }

        Product bundle = new Product();
        StringBuilder new_name = new StringBuilder();
        double totalPrice = 0.0;

        List<Product> sources = new ArrayList<>();
        for(Long id : ids){
            sources.add(getById(id)); // Using the improved getById method
        }

        if(!verifyIds(ids)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Some IDs are already in a bundle or are duplicates"
            );
        }

        for (Product product : sources) {
            new_name.append(product.getName()).append(" + ");
            totalPrice += product.getPrice();
        }

        // Remove the trailing " + "
        String bundleName = new_name.length() > 0 ? 
            new_name.substring(0, new_name.length() - 3) : "";
            
        bundle.setName(bundleName);
        bundle.setPrice(totalPrice);
        bundle.setSources(sources);

        return create(bundle);
    }
    
    private Boolean verifyIds(Long[] ids){
        List<Product> allBundles = getAll().stream()
                .filter(Product::isBundle)
                .toList();

        // Check if all products are already in an existing bundle
        for (Product bundle : allBundles) {
            List<Long> sourceIds = bundle.getSources().stream()
                    .map(Product::getId)
                    .toList();

            if (!Collections.disjoint(sourceIds, Arrays.asList(ids))) {
                return false;
            }
        }

        // Check for duplicate IDs
        return ids.length == Arrays.stream(ids).distinct().count();
    }
}