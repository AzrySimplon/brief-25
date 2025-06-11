package fr.azry.TPSpringBoot;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.azry.TPSpringBoot.controller.ProductController;
import fr.azry.TPSpringBoot.model.Product;
import fr.azry.TPSpringBoot.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;


@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    final private String URL = "http://localhost:8080/";


    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Initialize test products
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setPrice(10.0);

        testProduct2 = new Product();
        testProduct2.setName("Test Product 2");
        testProduct2.setPrice(20.0);

        repository.deleteAll();
    }

    // Test get all products
    @Test
    void getAllProducts() throws Exception {
        Product p1 = repository.save(testProduct);
        Product p2 = repository.save(testProduct2);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value(p1.getName()))
                .andExpect(jsonPath("$[0].price").value(p1.getPrice()))
                .andExpect(jsonPath("$[1].name").value(p2.getName()))
                .andExpect(jsonPath("$[1].price").value(p2.getPrice()));
    }


    //Test get 1 product by id
    @Test
    void getProductById() throws Exception {
        Product p = repository.save(testProduct);

        mockMvc.perform(get(URL + "products/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(containsString(p.getName())));
    }

    //Test get 1 product by id not found
    @Test
    void getProductByIdNotFound() throws Exception {
        mockMvc.perform(get(URL + "products/999"))
                .andExpect(status().isNotFound());
    }

    //Test create product
    @Test
    void createProduct() throws Exception {
        mockMvc.perform(post(URL + "products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(containsString(testProduct.getName())));
    }

    //Test update product
    @Test
    void updateProduct() throws Exception {
        Product p = repository.save(testProduct);


        mockMvc.perform(put(URL + "products/" + p.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p.getId()))
                .andExpect(jsonPath("$.name").value(containsString(testProduct2.getName())));
    }

    //Test delete product
    @Test
    void deleteProduct() throws Exception {
        Product p = repository.save(testProduct);

        mockMvc.perform(delete(URL + "products/" + p.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get(URL + "products/" + p.getId()))
                .andExpect(status().isNotFound());
    }

    //Test duplicate product
    @Test
    void duplicateProduct() throws Exception {
        Product p = repository.save(testProduct);

        mockMvc.perform(post(URL + "products/" + p.getId() + "/duplicate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(containsString(testProduct.getName() + " copy")));

        mockMvc.perform(get(URL + "products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    //Test create bundle
    @Test
    void createBundle() throws Exception {
        Product p1 = repository.save(testProduct);
        Product p2 = repository.save(testProduct2);

        mockMvc.perform(post(URL + "products/bundle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Long[]{p1.getId(), p2.getId()})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(containsString(testProduct.getName() + " + " + testProduct2.getName())))
                .andExpect(jsonPath("$.price").value(30.0))
                .andExpect(jsonPath("$.sources", hasSize(2)))
                .andExpect(jsonPath("$.sources[0].id").value(p1.getId()))
                .andExpect(jsonPath("$.sources[1].id").value(p2.getId()));
    }

    //Test create bundle with duplicate ids
    @Test
    void createBundleWithDuplicateIds() throws Exception {
        Product p1 = repository.save(testProduct);
        Product p2 = repository.save(testProduct2);

        mockMvc.perform(post(URL + "products/bundle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Long[]{p1.getId(), p2.getId(), p1.getId()})))
                .andExpect(status().isBadRequest());
    }

    //Test create bundle with product already in bundle
    @Test
    void createBundleWithProductAlreadyInBundle() throws Exception {
        repository.save(testProduct2);
        testProduct.setSources(List.of(testProduct2));

        repository.save(testProduct);

        mockMvc.perform(post(URL + "products/bundle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Long[]{testProduct2.getId()})))
                .andExpect(status().isBadRequest());
    }
}