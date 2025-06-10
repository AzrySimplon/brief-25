package fr.azry.TPSpringBoot.controller;


import fr.azry.TPSpringBoot.model.Product;
import fr.azry.TPSpringBoot.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

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
        return repository.findById(id).orElseThrow();
    }
    @PostMapping
    public Product create(@RequestBody Product product) {
        return repository.save(product);
    }
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = repository.findById(id).orElseThrow();
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        return repository.save(existing);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
    @PostMapping("/{id}/duplicate")
    public Product duplicate(@PathVariable Long id) {
        Product existing_product = repository.findById(id).orElseThrow();
        Product new_product = new Product();
        new_product.setName(existing_product.getName() + " copy");
        new_product.setPrice(existing_product.getPrice());
        return create(new_product);
    }

    @PostMapping("/bundle")
    public Product createBundle(@RequestBody Long[] ids) {
        Product bundle = new Product();
        StringBuilder new_name = new StringBuilder();
        double totalPrice = 0.0;

        List<Product> sources = new ArrayList<>();
        for(Long id : ids){
            sources.add(getById(id));
        }


        if(!verifyIds(ids)){
            throw new IllegalArgumentException("Some IDs are already in a bundle or are duplicates");
        }

        for (Product product : sources) {
            new_name.append(product.getName()).append(" + ");
            totalPrice += product.getPrice();
        }

        bundle.setName(new_name.toString());
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

            System.out.println(!Collections.disjoint(sourceIds, Arrays.asList(ids)));
            if (!Collections.disjoint(sourceIds, Arrays.asList(ids))) {
                return false;
            }
        }

        // Check for duplicate IDs
        return ids.length == Arrays.stream(ids).distinct().count();
    }
}