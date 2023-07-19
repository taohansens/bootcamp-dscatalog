package com.taohansen.dscatalog.services;

import com.taohansen.dscatalog.dto.ProductDTO;
import com.taohansen.dscatalog.entities.Category;
import com.taohansen.dscatalog.entities.Product;
import com.taohansen.dscatalog.repositories.CategoryRepository;
import com.taohansen.dscatalog.repositories.ProductRepository;
import com.taohansen.dscatalog.services.exceptions.DatabaseException;
import com.taohansen.dscatalog.services.exceptions.ResourceNotFoundException;
import com.taohansen.dscatalog.tests.Factory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {
    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;
    @Mock
    private CategoryRepository categoryRepository;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;
    private PageImpl<Product> page;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 20L;
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));

        Mockito.when(repository.existsById(existingId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);
        Mockito.when(repository.existsById(dependentId)).thenReturn(true);

        Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);

        Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
        Mockito.doThrow(EntityNotFoundException.class).when(repository).getReferenceById(nonExistingId);

        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.doNothing().when(repository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

        Mockito.when(categoryRepository.getReferenceById(ArgumentMatchers.any())).thenReturn(new Category());
    }

    @Test
    public void findByIdShouldReturnObjectWhenIdExists() {
        ProductDTO result = service.findById(existingId);
        Assertions.assertNotNull(result);
        //Depends Element on Factory.createProduct()
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals(result.getCategories().get(0).getName(), "Electronics");
    }

    @Test
    public void findByIdShouldThrowExceptionWhenIdNonExists() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
    }

    @Test
    public void findAllPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDTO> result = service.findAllPaged(pageable);

        Assertions.assertNotNull(result);
        Mockito.verify(repository).findAll(pageable);
    }

    @Test
    public void updateShouldReturnObjectWhenIdExists() {
        String UPDATED_NAME = "OK";
        ProductDTO dto = Factory.createProductDTO(UPDATED_NAME);
        ProductDTO result = service.update(existingId, dto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(UPDATED_NAME, result.getName());
    }

    @Test
    public void updateShouldThrowExceptionWhenIdNonExists() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.update(nonExistingId, Factory.createProductDTO()));
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdDependent() {
        Assertions.assertThrows(DatabaseException.class,
                () -> service.delete(dependentId));
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> service.delete(nonExistingId));
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> service.delete(existingId));
        Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
    }

}
