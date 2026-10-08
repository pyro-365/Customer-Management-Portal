package com.customer.controller;

import com.customer.dto.Requests;
import com.customer.model.CustomerOrder;
import com.customer.security.SessionUser;
import com.customer.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) { this.orders = orders; }

    @GetMapping
    public List<CustomerOrder> list(HttpSession s) { return orders.list(SessionUser.id(s)); }

    @PostMapping
    public List<CustomerOrder> checkout(@Valid @RequestBody Requests.Checkout r, HttpSession s) {
        return orders.checkout(SessionUser.id(s), r.items());
    }
}
