package com.vkeonline.payment.redis.repository;

import com.vkeonline.payment.redis.domain.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
