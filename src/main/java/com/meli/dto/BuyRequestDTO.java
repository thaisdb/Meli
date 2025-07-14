// package: com.meli.dto
package com.meli.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BuyRequestDTO {
    @NotNull(message = "BuyRequestDTO: Quantidade é obrigatória.")
    @Min(value = 1, message = "BuyRequestDTO: Quantidade deve ser pelo menos 1.")
    private int quantity;

    /*
     * Default constructor used by Jackson
     */

    public BuyRequestDTO() {
    }

    public BuyRequestDTO(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}