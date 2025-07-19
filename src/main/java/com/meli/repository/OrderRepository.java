package com.meli.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.meli.model.Orders;
import org.springframework.stereotype.Repository;

// REMOVIDO: import javax.annotation.PostConstruct;
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
    private List<Orders> orders;
    private final AtomicInteger idCounter;

    public OrderRepository() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        idCounter = new AtomicInteger(0);

        // Lógica de inicialização MOVIDA PARA O CONSTRUTOR
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
            orders.stream().mapToInt(Orders::getId).max().ifPresent(idCounter::set);
            idCounter.incrementAndGet(); 
            System.out.println("BACKEND: OrderRepository: Loaded " + orders.size() + " orders. Next ID will be: " + idCounter.get());
        }
        System.out.println("BACKEND: OrderRepository: --- Initialization complete. In-memory orders count: " + orders.size() + " ---");
    }

    // REMOVIDO: Método @PostConstruct init()

    private List<Orders> loadOrders() {
        System.out.println("BACKEND: OrderRepository: Attempting to load orders from: " + ORDER_FILE.getAbsolutePath());
        try {
            if (ORDER_FILE.exists()) {
                String fileContent = new String(Files.readAllBytes(Paths.get(ORDER_FILE.getAbsolutePath())));
                if (fileContent.trim().isEmpty() || fileContent.trim().equals("[]")) {
                    System.out.println("BACKEND: OrderRepository: orders.json is empty or contains only an empty array. Returning empty list.");
                    return new ArrayList<>();
                }
                List<Orders> loadedOrders = objectMapper.readValue(fileContent, new TypeReference<List<Orders>>() {});
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
            System.out.println("BACKEND: OrderRepository: Orders saved successfully. Total orders: " + orders.size());
        } catch (IOException e) {
            System.err.println("BACKEND: OrderRepository: ERROR saving orders to JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Orders save(Orders order) {
        if (order.getId() == 0) { 
            order.setId(idCounter.getAndIncrement());
            orders.add(order);
            System.out.println("BACKEND: OrderRepository.save(): Assigned new ID " + order.getId() + " to new order.");
        } else { 
            Optional<Orders> existingOrderOpt = getById(order.getId());
            if (existingOrderOpt.isPresent()) {
                Orders existingOrder = existingOrderOpt.get();
                existingOrder.setConsumerId(order.getConsumerId());
                existingOrder.setSellerId(order.getSellerId());
                existingOrder.setProducts(order.getProducts());
                existingOrder.setShippingAddress(order.getShippingAddress());
                existingOrder.setSendersAddress(order.getSendersAddress());
                existingOrder.setTotal(order.getTotal());
                existingOrder.setShippingCost(order.getShippingCost());
                existingOrder.setPaymentMethod(order.getPaymentMethod());
                existingOrder.setStatus(order.getStatus());
                System.out.println("BACKEND: OrderRepository.save(): Updated order ID " + order.getId() + ".");
            } else {
                System.err.println("BACKEND: OrderRepository.save(): Attempted to update non-existent order with ID: " + order.getId());
                return null;
            }
        }
        saveOrders();
        return order;
    }

    public Optional<Orders> getById(int id) {
        return orders.stream().filter(o -> o.getId() == id).findFirst();
    }

    public List<Orders> getAll() {
        return new ArrayList<>(orders);
    }
}
