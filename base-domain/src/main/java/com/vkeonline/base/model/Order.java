package com.vkeonline.base.model;

import lombok.Data;

@Data
public class Order {
    private Long id;
    private Long customerId;
    private Long productId;
    private int productCount;
    private int price;
    private String status;
    private String source;
}
