package com.meli.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // IMPORTANTE: Adicionar esta importação
import com.meli.model.Order;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class OrderRepository {

    private final File ORDER_FILE = new File("data/orders.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Order> orders;
    private final AtomicInteger idCounter;

    public OrderRepository() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // ADIÇÃO CRÍTICA: Registra o módulo para lidar com tipos de data/hora do Java 8+
        objectMapper.registerModule(new JavaTimeModule()); 
        // Se você já usa objectMapper.findAndRegisterModules() em algum lugar,
        // esta linha pode ser redundante, mas é a forma explícita de garantir.

        idCounter = new AtomicInteger(0);

        System.out.println("BACKEND: OrderRepository: --- Initializing Repository ---");
        if (!ORDER_FILE.getParentFile().exists()) {
            ORDER_FILE.getParentFile().mkdirs();
            System.out.println("BACKEND: OrderRepository: Created data directory: " + ORDER_FILE.getParentFile().getAbsolutePath());
        }
        orders = loadOrders();
        if (orders.isEmpty() && !ORDER_FILE.exists()) {
            System.out.println("BACKEND: OrderRepository: orders list is empty and file does not exist. Saving empty list to create file.");
            saveOrders();
            System.out.println("BACKEND: OrderRepository: Initialized empty orders.json file.");
        } else if (!orders.isEmpty()) {
            orders.stream().mapToInt(Order::getId).max().ifPresent(idCounter::set);
            idCounter.incrementAndGet(); 
            System.out.println("BACKEND: OrderRepository: Loaded " + orders.size() + " orders. Next ID will be: " + idCounter.get());
        }
        System.out.println("BACKEND: OrderRepository: --- Initialization complete. In-memory orders count: " + orders.size() + " ---");
    }

    private List<Order> loadOrders() {
        System.out.println("BACKEND: OrderRepository: Attempting to load orders from: " + ORDER_FILE.getAbsolutePath());
        try {
            if (ORDER_FILE.exists()) {
                String fileContent = new String(Files.readAllBytes(Paths.get(ORDER_FILE.getAbsolutePath())));
                if (fileContent.trim().isEmpty() || fileContent.trim().equals("[]")) {
                    System.out.println("BACKEND: OrderRepository: orders.json is empty or contains only an empty array. Returning empty list.");
                    return new ArrayList<>();
                }
                List<Order> loadedOrders = objectMapper.readValue(fileContent, new TypeReference<List<Order>>() {});
                System.out.println("BACKEND: OrderRepository: Successfully deserialized " + loadedOrders.size() + " orders from file.");
                return loadedOrders;
            } else {
                System.out.println("BACKEND: OrderRepository: orders.json does not exist. Returning an empty list.");
            }
        } catch (IOException e) {
            System.err.println("BACKEND: OrderRepository: ERROR loading orders from JSON file: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private synchronized void saveOrders() {
        System.out.println("BACKEND: OrderRepository: Attempting to save " + orders.size() + " orders to: " + ORDER_FILE.getAbsolutePath());
        try {
            objectMapper.writeValue(ORDER_FILE, orders);
            System.out.println("BACKEND: OrderRepository: Order saved successfully. Total orders: " + orders.size());
        } catch (IOException e) {
            System.err.println("BACKEND: OrderRepository: ERROR saving orders to JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Order save(Order order) {
        if (order.getId() == 0) { 
            order.setId(idCounter.getAndIncrement());
            orders.add(order);
            System.out.println("BACKEND: OrderRepository.save(): Assigned new ID " + order.getId() + " to new order.");
        } else { 
            Optional<Order> existingOrderOpt = getById(order.getId());
            if (existingOrderOpt.isPresent()) {
                Order existingOrder = existingOrderOpt.get();
                existingOrder.setConsumerId(order.getConsumerId());
                existingOrder.setSellerId(order.getSellerId());
                existingOrder.setProducts(order.getProducts());
                existingOrder.setShippingAddress(order.getShippingAddress());
                existingOrder.setSendersAddress(order.getSendersAddress());
                existingOrder.setTotal(order.getTotal());
                existingOrder.setShippingCost(order.getShippingCost());
                existingOrder.setPaymentMethod(order.getPaymentMethod());
                existingOrder.setStatus(order.getStatus());
                existingOrder.setTimestamp(order.getTimestamp());
                System.out.println("BACKEND: OrderRepository.save(): Updated order ID " + order.getId() + ".");
            } else {
                System.err.println("BACKEND: OrderRepository.save(): Attempted to update non-existent order with ID: " + order.getId());
                return null;
            }
        }
        saveOrders();
        return order;
    }

    public Optional<Order> getById(int id) {
        return orders.stream().filter(o -> o.getId() == id).findFirst();
    }

    public List<Order> getAll() {
        return new ArrayList<>(orders);
    }
    // Adicione o método deleteById se ele existia antes e foi removido
    public boolean deleteById(int id) {
        boolean removed = orders.removeIf(o -> o.getId() == id);
        if (removed) {
            saveOrders();
        }
        return removed;
    }
}
