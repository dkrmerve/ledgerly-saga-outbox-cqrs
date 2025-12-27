package com.example.ledgerly.api.dto;

import javax.validation.constraints.Min;

public class AdjustStockRequest {
    @Min(0) public int available;
}
