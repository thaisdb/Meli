// package: com.meli.dto
package com.meli.dto;

/*
 * Contêiner para dados que são transferidos entre o frontend e o backend,
 * especificamente para representar um item que um consumidor deseja comprar.
 * Ele desacopla o modelo de dados interno (Product) da estrutura de requisição externa.
 */
public class BuyRequestDTO {
    private Integer id;
    private Integer quantity;

    public BuyRequestDTO() {
    }

    /*
     * Armazena o id do produto e a quantidade desejada respectiva
     * @param Id do produto
     * @param quantidade
     */
    public BuyRequestDTO(Integer id, Integer quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
