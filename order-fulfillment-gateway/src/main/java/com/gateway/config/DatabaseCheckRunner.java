package com.gateway.config;

import com.gateway.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseCheckRunner implements CommandLineRunner {

    private final DataSource dataSource;
    private final OrderRepository orderRepository;

    public DatabaseCheckRunner(DataSource dataSource, OrderRepository orderRepository) {
        this.dataSource = dataSource;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println(">>> Testing NeonDB Database Connection...");
        try (Connection connection = dataSource.getConnection()) {
            System.out.println(">>> Connection SUCCESS! Connected to: " + connection.getMetaData().getDatabaseProductName());
            System.out.println(">>> Schema Version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println(">>> Existing orders in DB: " + orderRepository.count());
        } catch (Exception ex) {
            System.err.println(">>> NeonDB Connection FAILED: " + ex.getMessage());
        }
        System.out.println("=================================================");
    }
}