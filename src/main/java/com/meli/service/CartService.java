package com.meli.service;

import com.meli.model.Consumer;
import com.meli.model.Product;
import com.meli.model.User;
import com.meli.repository.UserRepository;
import com.meli.dto.BuyRequestDTO;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private final UserRepository userRepository;
    private final ProductService productService;

    public CartService(UserRepository userRepository, ProductService productService) {
        this.userRepository = userRepository;
        this.productService = productService;
    }

    /**
     * Tenta obter um Consumer a partir de um ID de usuário.
     * Utiliza userRepository.getById(userId) que retorna User ou null.
     * @param userId ID do usuário.
     * @return Optional de Consumer se o usuário for encontrado e for um Consumer, Optional.empty() caso contrário.
     */
    private Optional<Consumer> getConsumerById(int userId) {
        User user = userRepository.getById(userId); // Retorna User ou null
        if (user == null) {
            System.err.println("ERROR: CartService - getConsumerById: Usuário com ID " + userId + " não encontrado no repositório.");
            return Optional.empty();
        }
        if (!(user instanceof Consumer)) {
            System.err.println("ERROR: CartService - getConsumerById: Usuário com ID " + userId + " encontrado, mas não é do tipo Consumer. Tipo: " + user.getClass().getSimpleName());
            return Optional.empty();
        }
        return Optional.of((Consumer) user);
    }

    /**
     * Adiciona uma quantidade de um produto ao carrinho do consumidor.
     * Utiliza o método addProductToCart do Consumer.
     * @param consumerId ID do consumidor.
     * @param productId ID do produto a ser adicionado.
     * @param quantity Quantidade a ser adicionada ao produto no carrinho.
     * @return Optional do Consumer atualizado se bem-sucedido, Optional.empty() caso contrário.
     */
    public Optional<Consumer> addProductToCart(int consumerId, int productId, int quantity) {
        Optional<Consumer> optionalConsumer = getConsumerById(consumerId);
        if (optionalConsumer.isEmpty()) {
            System.err.println("ERROR: CartService.addProductToCart: Falha ao obter consumidor com ID " + consumerId + ".");
            return Optional.empty();
        }

        Consumer consumer = optionalConsumer.get();
        Product product = productService.getProductById(productId);

        if (product == null) {
            System.err.println("ERROR: CartService.addProductToCart: Produto com ID " + productId + " não encontrado.");
            return Optional.empty();
        }

        // Validação de estoque: Verificar se a adição da quantidade excede o estoque disponível
        int currentQuantityInCart = consumer.getCart().getOrDefault(productId, 0);
        if (product.getStock() == null || product.getStock() < (currentQuantityInCart + quantity)) {
            System.err.println("ERROR: CartService.addProductToCart: Estoque insuficiente para o produto " + product.getTitle() + " (ID: " + productId + "). Disponível: " + product.getStock() + ", Solicitado (total): " + (currentQuantityInCart + quantity) + ".");
            return Optional.empty(); 
        }

        consumer.addProductToCart(productId, quantity);
        userRepository.save(consumer);
        System.out.println("DEBUG: CartService.addProductToCart: Adicionado " + quantity + " unidades do produto " + product.getTitle() + " (ID: " + productId + ") ao carrinho do consumidor " + consumerId + ". Nova quantidade total: " + consumer.getCart().get(productId));
        return Optional.of(consumer);
    }

    /**
     * Define a quantidade de um produto no carrinho de um consumidor para um valor específico.
     * @param consumerId ID do consumidor.
     * @param productId ID do produto.
     * @param newQuantity Nova quantidade total para o produto no carrinho.
     * @return Optional do Consumer atualizado se bem-sucedido, Optional.empty() caso contrário.
     */
    public Optional<Consumer> setProductQuantityInCart(int consumerId, int productId, int newQuantity) {
        Optional<Consumer> optionalConsumer = getConsumerById(consumerId);
        if (optionalConsumer.isEmpty()) {
            System.err.println("ERROR: CartService.setProductQuantityInCart: Falha ao obter consumidor com ID " + consumerId + ".");
            return Optional.empty();
        }

        Consumer consumer = optionalConsumer.get();
        Product product = productService.getProductById(productId);

        if (product == null) {
            System.err.println("ERROR: CartService.setProductQuantityInCart: Produto com ID " + productId + " não encontrado.");
            return Optional.empty();
        }

        if (newQuantity <= 0) {
            consumer.removeProductFromCart(productId);
            System.out.println("DEBUG: CartService.setProductQuantityInCart: Produto " + productId + " removido do carrinho do consumidor " + consumerId + " (quantidade definida para 0).");
        } else {
            if (product.getStock() == null || product.getStock() < newQuantity) {
                System.err.println("ERROR: CartService.setProductQuantityInCart: Estoque insuficiente para o produto " + product.getTitle() + " (ID: " + productId + "). Disponível: " + product.getStock() + ", Solicitado (total): " + newQuantity + ".");
                return Optional.empty(); 
            }
            consumer.getCart().put(productId, newQuantity);
            System.out.println("DEBUG: CartService.setProductQuantityInCart: Quantidade do produto " + product.getTitle() + " (ID: " + productId + ") definida para " + newQuantity + " no carrinho do consumidor " + consumerId);
        }
        
        userRepository.save(consumer);
        return Optional.of(consumer);
    }

    /**
     * Remove um produto do carrinho de um consumidor.
     * Utiliza o método removeProductFromCart do Consumer.
     * @param consumerId ID do consumidor.
     * @param productId ID do produto a ser removido.
     * @return Optional do Consumer atualizado se bem-sucedido, Optional.empty() caso contrário.
     */
    public Optional<Consumer> removeProductFromCart(int consumerId, int productId) {
        Optional<Consumer> optionalConsumer = getConsumerById(consumerId);
        if (optionalConsumer.isEmpty()) {
            System.err.println("ERROR: CartService.removeProductFromCart: Falha ao obter consumidor com ID " + consumerId + ".");
            return Optional.empty();
        }

        Consumer consumer = optionalConsumer.get();
        consumer.removeProductFromCart(productId);
        userRepository.save(consumer);
        System.out.println("DEBUG: CartService.removeProductFromCart: Produto com ID " + productId + " removido do carrinho do consumidor " + consumerId);
        return Optional.of(consumer);
    }

    /**
     * Obtém o carrinho de um consumidor, incluindo detalhes completos dos produtos.
     * @param consumerId ID do consumidor.
     * @return Optional de uma lista de mapas (detalhes do produto + quantidade no carrinho) se encontrado, Optional.empty() caso contrário.
     */
    public Optional<List<Map<String, Object>>> getDetailedCart(int consumerId) {
        Optional<Consumer> optionalConsumer = getConsumerById(consumerId);
        if (optionalConsumer.isEmpty()) {
            System.err.println("ERROR: CartService.getDetailedCart: Falha ao obter consumidor com ID " + consumerId + ".");
            return Optional.empty();
        }

        Consumer consumer = optionalConsumer.get();
        Map<Integer, Integer> cartProducts = consumer.getCart();
        List<Map<String, Object>> detailedCartItems = new ArrayList<>();

        // Usar um iterador para permitir remoção segura durante a iteração
        cartProducts.entrySet().removeIf(entry -> {
            Integer productId = entry.getKey();
            Integer quantityInCart = entry.getValue();
            Product product = productService.getProductById(productId);

            if (product != null) {
                Map<String, Object> itemDetails = new HashMap<>();
                itemDetails.put("id", product.getId());
                itemDetails.put("title", product.getTitle());
                itemDetails.put("imageUrl", product.getImageUrl());
                itemDetails.put("price", product.getPrice());
                itemDetails.put("stock", product.getStock());
                itemDetails.put("quantity", quantityInCart);
                detailedCartItems.add(itemDetails);
                return false; // Não remover
            } else {
                System.err.println("WARN: CartService.getDetailedCart: Produto com ID " + productId + " no carrinho do consumidor " + consumerId + " não encontrado no ProductService. Removendo do carrinho.");
                return true; // Remover este item inválido
            }
        });
        userRepository.save(consumer);
        return Optional.of(detailedCartItems);
    }

    /**
     * Limpa todos os produtos do carrinho de um consumidor.
     * @param consumerId ID do consumidor.
     * @return Optional do Consumer atualizado se bem-sucedido, Optional.empty() caso contrário.
     */
    public Optional<Consumer> clearCart(int consumerId) {
        Optional<Consumer> optionalConsumer = getConsumerById(consumerId);
        if (optionalConsumer.isEmpty()) {
            System.err.println("ERROR: CartService.clearCart: Falha ao obter consumidor com ID " + consumerId + ".");
            return Optional.empty();
        }

        Consumer consumer = optionalConsumer.get();
        consumer.getCart().clear();
        userRepository.save(consumer);
        System.out.println("DEBUG: CartService.clearCart: Carrinho do consumidor " + consumerId + " limpo.");
        return Optional.of(consumer);
    }
}
