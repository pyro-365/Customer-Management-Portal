package com.customer.controller;

import com.customer.dto.Views;
import com.customer.security.SessionUser;
import com.customer.service.CustomerService;
import com.customer.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CustomerService customers;
    private final OrderService orders;

    public DashboardController(CustomerService customers, OrderService orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @GetMapping
    public Views.Dashboard get(HttpSession s) {
        Long id = SessionUser.id(s);
        return new Views.Dashboard(customers.count(), orders.count(id), orders.totalSpent(id), orders.recent(id));
    }
}
