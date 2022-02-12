package com.vkeonline.payment.repository;

import com.vkeonline.payment.domain.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
