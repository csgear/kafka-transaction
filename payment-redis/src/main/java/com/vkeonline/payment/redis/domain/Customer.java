package com.vkeonline.payment.redis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;


@RedisHash("Customer")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {
    private Long id;
    private String name;
    private int amountAvailable;
    private int amountReserved;

}
