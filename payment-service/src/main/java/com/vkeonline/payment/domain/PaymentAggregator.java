package com.vkeonline.payment.domain;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentAggregator {
    private int amountAvailable;
    private int amountReserved;

    public PaymentAggregator(int amountAvailable) {
        this.amountAvailable = amountAvailable;
    }
}
