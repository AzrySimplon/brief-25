package fr.azry.TPSpringBoot;

import fr.azry.TPSpringBoot.controller.ProductController;
import fr.azry.TPSpringBoot.model.Product;
import fr.azry.TPSpringBoot.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductController controller;

    private Product testProduct;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test products
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(10.0);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Test Product 2");
        testProduct2.setPrice(20.0);
    }

    @Test
    void getAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(repository.findAll()).thenReturn(products);

        // Act
        List<Product> result = controller.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void getProductById() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = controller.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(repository).findById(1L);
    }

    @Test
    void getProductByIdNotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> controller.getById(999L));
    }

    @Test
    void createProduct() {
        // Arrange
        when(repository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = controller.create(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(repository).save(any(Product.class));
    }

    @Test
    void updateProduct() {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(15.0);

        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = controller.update(1L, updatedProduct);

        // Assert
        assertEquals("Updated Product", result.getName());
        assertEquals(15.0, result.getPrice());
        verify(repository).save(any(Product.class));
    }

    @Test
    void deleteProduct() {
        // Arrange
        doNothing().when(repository).deleteById(1L);

        // Act
        controller.delete(1L);

        // Assert
        verify(repository).deleteById(1L);
    }

    @Test
    void duplicateProduct() {
        // Arrange
        Product duplicatedProduct = new Product();
        duplicatedProduct.setName("Test Product copy");
        duplicatedProduct.setPrice(10.0);

        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.save(any(Product.class))).thenReturn(duplicatedProduct);

        // Act
        Product result = controller.duplicate(1L);

        // Assert
        assertEquals("Test Product copy", result.getName());
        assertEquals(10.0, result.getPrice());
        verify(repository).save(any(Product.class));
    }

    @Test
    void createBundleSuccess() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.findById(2L)).thenReturn(Optional.of(testProduct2));
        when(repository.findAll()).thenReturn(Arrays.asList(testProduct, testProduct2));
        
        Product expectedBundle = new Product();
        expectedBundle.setName("Test Product + Test Product 2 + ");
        expectedBundle.setPrice(30.0);
        expectedBundle.setSources(Arrays.asList(testProduct, testProduct2));
        
        when(repository.save(any(Product.class))).thenReturn(expectedBundle);

        // Act
        Product result = controller.createBundle(new Long[]{1L, 2L});

        // Assert
        assertNotNull(result);
        assertEquals(30.0, result.getPrice());
        assertEquals("Test Product + Test Product 2 + ", result.getName());
        assertTrue(result.isBundle());
    }

    @Test
    void createBundleWithDuplicateIds() {
        // Arrange
        Long[] duplicateIds = new Long[]{1L, 1L};
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.findAll()).thenReturn(List.of(testProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.createBundle(duplicateIds));
    }

    @Test
    void createBundleWithProductAlreadyInBundle() {
        // Arrange
        Product bundle = new Product();
        bundle.setSources(List.of(testProduct));
        
        when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(repository.findAll()).thenReturn(List.of(bundle));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.createBundle(new Long[]{1L}));
    }
}