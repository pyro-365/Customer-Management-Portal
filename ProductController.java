package com.customer.controller;

import com.customer.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import com.customer.model.Product;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository products;

    public ProductController(ProductRepository products) { this.products = products; }

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "") String q,
                                    @RequestParam(defaultValue = "") String category,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "24") int size) {
        Page<Product> result = products.search(q.trim(), category.trim(),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 60), Sort.by("id")));
        return Map.of("items", result.getContent(),
                "page", result.getNumber(),
                "totalPages", result.getTotalPages(),
                "total", result.getTotalElements());
    }

    @GetMapping("/categories")
    public List<String> categories() { return products.categories(); }
}
