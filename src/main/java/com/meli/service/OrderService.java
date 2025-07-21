package com.meli.service;

import com.meli.model.Consumer;
import com.meli.model.Order;
import com.meli.model.Product;
import com.meli.model.OrderStatus;
import com.meli.model.PaymentMethod;
import com.meli.model.Seller;
import com.meli.model.User;
import com.meli.repository.OrderRepository;
import com.meli.repository.UserRepository;
import com.meli.dto.BuyRequestDTO;
import com.meli.dto.OrderProductDetailDTO;
import com.meli.dto.OrderSummaryDTO;
import com.meli.dto.SellerOrderDTO;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Contém a lógica de negócio para a criação e gerenciamento de pedidos.
 * Ele orquestra as operações necessárias para transformar os itens do carrinho em um pedido final
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, UserService userService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
    }

    /**
     * Cria um novo pedido de produto.
     * Este método agora lida com a lógica de um único pedido por vez,
     * determinando o vendedor e os detalhes do pedido.
     * @param purchaseItems Lista de BuyRequestDTOs (productId e quantity).
     * @param consumerId ID do consumidor que está fazendo a compra.
     * @return Optional de Order se o pedido for criado com sucesso, Optional.empty() caso contrário.
     */
    public Optional<Order> createProductOrder(List<BuyRequestDTO> purchaseItems, int consumerId) {
        // Validação do consumidor
        User user = userService.getUserById(consumerId);
        if (user == null || !(user instanceof Consumer)) {
            System.err.println("ERROR: OrderService - Consumidor com ID " + consumerId + " não encontrado ou não é do tipo Consumidor.");
            return Optional.empty();
        }
        Consumer consumer = (Consumer) user;

        // Agrupa produtos por vendedor para criar um pedido por vendedor
        Map<Integer, List<BuyRequestDTO>> itemsBySeller = new HashMap<>();
        for (BuyRequestDTO item : purchaseItems) {
            Product product = productService.getProductById(item.getId());
            if (product == null) {
                System.err.println("ERROR: OrderService - Produto com ID " + item.getId() + " não encontrado durante a criação do pedido.");
                return Optional.empty(); // Ou lidar com erro de forma diferente
            }
            itemsBySeller.computeIfAbsent(product.getSellerId(), k -> new ArrayList<>()).add(item);
        }

        Order createdOrder = null;

        for (Map.Entry<Integer, List<BuyRequestDTO>> entry : itemsBySeller.entrySet()) {
            int sellerId = entry.getKey();
            List<BuyRequestDTO> sellerSpecificItems = entry.getValue();

            // Validação do vendedor
            User sellerUser = userService.getUserById(sellerId);
            if (sellerUser == null || !(sellerUser instanceof Seller)) {
                System.err.println("ERROR: OrderService - Vendedor com ID " + sellerId + " não encontrado ou não é do tipo Vendedor.");
                return Optional.empty();
            }

            Map<Integer, Integer> productsInOrder = new HashMap<>();
            double totalAmount = 0.0;

            for (BuyRequestDTO item : sellerSpecificItems) {
                Product product = productService.getProductById(item.getId());
                productsInOrder.put(item.getId(), item.getQuantity());
                totalAmount += product.getPrice() * item.getQuantity(); // Usar preço atual do produto
            }

            Order newOrder = new Order();
            newOrder.setConsumerId(consumerId);
            newOrder.setSellerId(sellerId);
            newOrder.setProducts(productsInOrder);
            newOrder.setTimestamp(ZonedDateTime.now());
            newOrder.setTotal(totalAmount);
            newOrder.setShippingCost(0.0); // Assumindo custo de envio 0 por padrão
            newOrder.setPaymentMethod(consumer.getPreferredPaymentMethod());
            newOrder.setStatus(OrderStatus.COMPLETED);

            orderRepository.save(newOrder);
            System.out.println("DEBUG: OrderService - Pedido criado com sucesso para o consumidor " + consumerId + " e vendedor " + sellerId + ". ID do pedido: " + newOrder.getId());
            createdOrder = newOrder;
        }
        
        return Optional.ofNullable(createdOrder);
    }

    /**
     * Obtém todos os pedidos.
     * @return Lista de todos os pedidos.
     */
    public List<Order> getAllOrders() {
        return orderRepository.getAll();
    }

    /**
     * Obtém um pedido pelo seu ID.
     * @param id ID do pedido.
     * @return Optional de Order se encontrado, Optional.empty() caso contrário.
     */
    public Optional<Order> getOrderById(int id) {
        return orderRepository.getById(id);
    }

    /**
     * Obtém pedidos de um consumidor específico e os converte para OrderSummaryDTO.
     * Inclui a lista de detalhes dos produtos (OrderProductDetailDTO) e a contagem de itens no DTO.
     * @param consumerId O ID do consumidor.
     * @return Uma lista de OrderSummaryDTOs feitos pelo consumidor.
     */
    public List<OrderSummaryDTO> getOrdersByConsumerId(int consumerId) {
        System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Buscando pedidos para o consumidor ID: " + consumerId);
        List<Order> consumerOrders = orderRepository.getAll().stream()
                                            .filter(order -> order.getConsumerId() == consumerId)
                                            .collect(Collectors.toList());

        List<OrderSummaryDTO> responseDTOs = new ArrayList<>();
        for (Order order : consumerOrders) {
            System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Processando pedido ID: " + order.getId() + ", Consumer ID: " + order.getConsumerId());
            System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Produtos no pedido (mapa): " + order.getProducts());

            // Calcula itemCount a partir do mapa de produtos
            int itemCount = order.getProducts().values().stream().mapToInt(Integer::intValue).sum();
            
            // Constrói a lista de OrderProductDetailDTOs
            List<OrderProductDetailDTO> productsDetails = new ArrayList<>();
            if (order.getProducts() != null && !order.getProducts().isEmpty()) {
                for (Map.Entry<Integer, Integer> entry : order.getProducts().entrySet()) {
                    Integer productId = entry.getKey();
                    Integer quantity = entry.getValue();
                    System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Buscando detalhes para Produto ID: " + productId + ", Quantidade: " + quantity);

                    Product product = productService.getProductById(productId); // Busca o produto para obter detalhes

                    if (product != null) {
                        System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Produto encontrado: " + product.getTitle());
                        productsDetails.add(new OrderProductDetailDTO(
                            product.getId(),
                            product.getTitle(),
                            quantity,
                            product.getImageUrl()
                        ));
                    } else {
                        System.err.println("WARNING: OrderService.getOrdersByConsumerId - Produto com ID " + productId + " NÃO ENCONTRADO no ProductService para o pedido " + order.getId() + ". Usando placeholder.");
                        productsDetails.add(new OrderProductDetailDTO(
                            productId,
                            "Produto Desconhecido (ID: " + productId + ")", // Adiciona o ID para depuração no frontend
                            quantity,
                            "https://placehold.co/40x40/e0e0e0/333333?text=N/A"
                        ));
                    }
                }
            } else {
                System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Mapa de produtos vazio ou nulo para o pedido ID: " + order.getId());
            }


            responseDTOs.add(new OrderSummaryDTO(
                order.getId(),
                order.getConsumerId(),
                order.getSellerId(),
                productsDetails, // Passa a lista de detalhes dos produtos
                itemCount, // Passa a contagem de itens
                order.getShippingAddress(),
                order.getTotal(),
                order.getShippingCost(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getTimestamp()
            ));
        }

        System.out.println("DEBUG: OrderService.getOrdersByConsumerId - Encontrados " + responseDTOs.size() + " pedidos (DTOs de resumo) para o consumidor ID: " + consumerId);
        return responseDTOs;
    }

    /**
     * Obtém pedidos que contêm produtos de um vendedor específico.
     * Retorna uma lista de SellerOrderDTOs, que incluem apenas os itens do vendedor em cada pedido.
     * @param sellerId ID do vendedor.
     * @return Lista de SellerOrderDTOs.
     */
    public List<SellerOrderDTO> getOrdersBySellerId(int sellerId) {
        List<Order> allOrders = orderRepository.getAll();
        List<SellerOrderDTO> sellerOrders = new ArrayList<>();

        for (Order order : allOrders) {
            // Filtra apenas os pedidos onde este vendedor é o sellerId
            if (order.getSellerId() != sellerId) {
                continue;
            }

            List<Map<String, Object>> sellerItemsInOrder = new ArrayList<>();
            double currentOrderSellerAmount = 0.0;

            // Itera sobre os produtos dentro do mapa 'products' do pedido
            for (Map.Entry<Integer, Integer> entry : order.getProducts().entrySet()) {
                Integer productId = entry.getKey();
                Integer quantity = entry.getValue();
                Product product = productService.getProductById(productId);

                // Verifica se o produto existe (já sabemos que pertence a este vendedor pelo filtro acima)
                if (product != null) {
                    Map<String, Object> itemDetails = new HashMap<>();
                    itemDetails.put("productId", product.getId());
                    itemDetails.put("title", product.getTitle());
                    itemDetails.put("quantity", quantity);
                    itemDetails.put("priceAtPurchase", product.getPrice());
                    itemDetails.put("totalItemAmount", quantity * product.getPrice());
                    sellerItemsInOrder.add(itemDetails);
                    currentOrderSellerAmount += (quantity * product.getPrice());
                }
            }

            // Se o pedido contém produtos deste vendedor, adiciona-o à lista de pedidos do vendedor
            if (!sellerItemsInOrder.isEmpty()) {
                sellerOrders.add(new SellerOrderDTO(
                    order.getId(),
                    order.getConsumerId(),
                    order.getTimestamp(),
                    order.getStatus().name(),
                    currentOrderSellerAmount, // MUDANÇA AQUI: Usar currentOrderSellerAmount
                    sellerItemsInOrder
                ));
            }
        }
        System.out.println("DEBUG: OrderService.getOrdersBySellerId - Encontrados " + sellerOrders.size() + " pedidos para o vendedor ID: " + sellerId);
        return sellerOrders;
    }
}
