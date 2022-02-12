package com.vkeonline.stock.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockAggregator {
    private int itemsAvailable;
    private int itemsReserved;

    public StockAggregator(int itemsAvailable) {
        this.itemsAvailable = itemsAvailable;
    }
}
