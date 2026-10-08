package com.customer.service;

import com.customer.dto.Requests;
import com.customer.exception.ApiException;
import com.customer.model.CustomerOrder;
import com.customer.model.Product;
import com.customer.repository.OrderRepository;
import com.customer.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    public List<CustomerOrder> list(Long customerId) { return orders.findByCustomerIdOrderByIdDesc(customerId); }

    public List<CustomerOrder> recent(Long customerId) { return orders.findTop5ByCustomerIdOrderByIdDesc(customerId); }

    public long count(Long customerId) { return orders.countByCustomerId(customerId); }

    public BigDecimal totalSpent(Long customerId) {
        return list(customerId).stream()
                .map(o -> o.getPrice().multiply(BigDecimal.valueOf(o.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Prices come from the database, never from the browser. All lines succeed or none do. */
    @Transactional
    public List<CustomerOrder> checkout(Long customerId, List<Requests.CartLine> lines) {
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        for (Requests.CartLine l : lines) wanted.merge(l.productId(), l.quantity(), Integer::sum);

        List<CustomerOrder> placed = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : wanted.entrySet()) {
            Product p = products.findForUpdate(e.getKey())
                    .orElseThrow(() -> new ApiException(404, "A product in your cart no longer exists."));
            if (p.getStock() < e.getValue())
                throw new ApiException(409, "Only " + p.getStock() + " left of " + p.getName() + ".");
            p.setStock(p.getStock() - e.getValue());

            CustomerOrder o = new CustomerOrder();
            o.setCustomerId(customerId);
            o.setProductId(p.getId());
            o.setProductName(p.getName());
            o.setImageUrl(p.getImageUrl());
            o.setQuantity(e.getValue());
            o.setPrice(p.getPrice());
            o.setOrderDate(LocalDate.now());
            o.setStatus("Processing");
            placed.add(orders.save(o));
        }
        return placed;
    }
}
