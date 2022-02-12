package com.vkeonline.stock.repository;

import com.vkeonline.stock.domain.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}
