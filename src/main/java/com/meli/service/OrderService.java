package com.meli.service;

import com.meli.model.Consumer;
import com.meli.model.Orders;
import com.meli.model.Product;
import com.meli.model.Seller;
import com.meli.model.OrderStatus; 
import com.meli.repository.OrderRepository;
import com.meli.dto.BuyRequestDTO; 
import com.meli.model.PaymentMethod; 
import com.meli.model.User; // Certifique-se de que User está importado para o cast

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors; // Pode ser útil, mas não estritamente necessário para esta lógica

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService; 
    private final UserService userService; 

    public OrderService(OrderRepository orderRepository, ProductService productService, UserService userService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.userService = userService;
    }

   /**
     * Cria um novo pedido com base nos itens fornecidos e no ID do consumidor.
     * Esta função é responsável APENAS pela criação e salvamento do objeto Orders.
     * As validações de estoque e decremento devem ser realizadas ANTES de chamar este método.
     * Assume que todos os produtos em um único pedido são do mesmo vendedor para simplificar o objeto Orders.
     *
     * @param itemsToPurchase Lista de BuyRequestDTO contendo productId e quantity para cada item a ser comprado.
     * @param consumerId ID do consumidor que está realizando a compra.
     * @return Optional de Orders se o pedido for criado com sucesso, Optional.empty() caso contrário (com erros logados).
     */
    public Optional<Orders> createProductOrder(List<BuyRequestDTO> itemsToPurchase, int consumerId) {
        if (itemsToPurchase == null || itemsToPurchase.isEmpty()) {
            System.err.println("ERROR: OrderService - Nenhum item fornecido para criação do pedido.");
            return Optional.empty();
        }

        // 1. Buscar o Consumidor
        User consumerUser = userService.getUserById(consumerId);
        if (!(consumerUser instanceof Consumer)) {
            System.err.println("ERROR: OrderService - Consumidor com ID " + consumerId + " não encontrado ou não é do tipo Consumidor.");
            return Optional.empty();
        }
        Consumer consumer = (Consumer) consumerUser;

        // Mapas e variáveis para consolidar os dados do pedido
        Map<Integer, Integer> productsMap = new HashMap<>(); // <productId, quantity> para o objeto Orders
        Double totalOrderPrice = 0.0;
        Integer commonSellerId = null; 
        String commonSendersAddress = null; 

        // Coleta de dados dos produtos para a criação do Order
        for (BuyRequestDTO itemDto : itemsToPurchase) {
            Product product = productService.getProductById(itemDto.getId());
            if (product == null) {
                // Isso não deveria acontecer se o ProductController validou, mas é um fallback
                System.err.println("ERROR: OrderService - Produto com ID " + itemDto.getId() + " não encontrado durante a criação do pedido. Validação prévia falhou?");
                return Optional.empty(); 
            }

            // Validação: Todos os produtos devem ser do mesmo vendedor para este modelo de Orders
            if (commonSellerId == null) {
                commonSellerId = product.getSellerId();
                User sellerUser = userService.getUserById(commonSellerId);
                if (!(sellerUser instanceof Seller)) {
                    System.err.println("ERROR: OrderService - Vendedor com ID " + commonSellerId + " (do produto " + product.getTitle() + ") não encontrado ou não é do tipo Vendedor. Validação prévia falhou?");
                    return Optional.empty();
                }
                commonSendersAddress = ((Seller) sellerUser).getAddress();
            } else if (!commonSellerId.equals(product.getSellerId())) {
                // Isso não deveria acontecer se o ProductController validou, mas é um fallback
                System.err.println("ERROR: OrderService - Pedido contém produtos de vendedores diferentes. Validação prévia falhou?");
                return Optional.empty(); 
            }

            productsMap.put(product.getId(), itemDto.getQuantity());
            totalOrderPrice += product.getPrice() * itemDto.getQuantity();
            // A atualização de estoque NÃO é feita aqui
        }

        // 2. Criar o Objeto Orders
        Orders newOrder = new Orders();
        newOrder.setConsumerId(consumerId);
        newOrder.setSellerId(commonSellerId); 
        newOrder.setProducts(productsMap);
        newOrder.setShippingAddress(consumer.getAddress()); 
        newOrder.setSendersAddress(commonSendersAddress); 
        newOrder.setTotal(totalOrderPrice);
        newOrder.setShippingCost(0.00); 
        newOrder.setPaymentMethod(consumer.getPreferredPaymentMethod()); 
        newOrder.setStatus(OrderStatus.PLACED); 

        // 3. Salvar o Pedido no Repositório
        Orders savedOrder = orderRepository.save(newOrder);

        System.out.println("DEBUG: OrderService - Pedido criado com sucesso! ID: " + savedOrder.getId() + " para o consumidor: " + consumerId);
        return Optional.of(savedOrder);
    }

    // Métodos adicionais para OrderService (se necessário no futuro)
    public Optional<Orders> getOrderById(int id) {
        return orderRepository.getById(id);
    }

    public List<Orders> getAllOrders() {
        return orderRepository.getAll();
    }
}
