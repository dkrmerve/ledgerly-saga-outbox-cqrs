package com.example.ledgerly.api.dto;

import javax.validation.constraints.*;
import java.util.List;

public class CreateOrderRequest {
    @NotBlank public String externalOrderId;

    @NotBlank public String currency; // ISO-4217, e.g. "EUR"

    @NotEmpty public List<Item> items;

    public static class Item {
        @NotBlank public String sku;
        @Min(1) public int quantity;
        @DecimalMin("0.01") public double unitAmount;
    }
}
