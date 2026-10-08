package com.customer.service;

import com.customer.dto.Requests;
import com.customer.model.Customer;
import com.customer.model.CustomerOrder;
import com.customer.model.Product;
import com.customer.repository.ProductRepository;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Gives Gemini the real catalogue and the customer's orders so it does not invent answers. */
@Service
public class AssistantService {

    private final GeminiService gemini;
    private final ProductRepository products;
    private final OrderService orders;

    public AssistantService(GeminiService gemini, ProductRepository products, OrderService orders) {
        this.gemini = gemini;
        this.products = products;
        this.orders = orders;
    }

    public String reply(Customer customer, Requests.Chat chat) {
        return gemini.ask(buildContext(customer, chat.message()), chat.history(), chat.message());
    }

    private String buildContext(Customer c, String question) {
        StringBuilder sb = new StringBuilder()
                .append("You are the shopping assistant of a customer portal. The customer is ")
                .append(c.getName()).append(". Prices are in Indian rupees (INR). ")
                .append("Answer only from the data below. If the data does not cover the question, say so. ")
                .append("Keep answers short and friendly.\n\nPRODUCTS THAT MATCH THE QUESTION:\n");

        Map<Long, Product> found = new LinkedHashMap<>();
        Arrays.stream(question.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3).limit(4)
                .forEach(w -> products.findTop10ByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(w, w)
                        .forEach(p -> found.putIfAbsent(p.getId(), p)));
        if (found.isEmpty()) sb.append("(no matching products)\n");
        found.values().stream().limit(12).forEach(p -> sb.append("- ").append(p.getName())
                .append(" | Rs ").append(p.getPrice()).append(" | stock ").append(p.getStock())
                .append(" | ").append(p.getCategory()).append("\n"));

        sb.append("\nTHE CUSTOMER'S LATEST ORDERS:\n");
        var recent = orders.recent(c.getId());
        if (recent.isEmpty()) sb.append("(no orders yet)\n");
        for (CustomerOrder o : recent) sb.append("- Order ").append(o.getId()).append(": ")
                .append(o.getProductName()).append(" x").append(o.getQuantity())
                .append(" | ").append(o.getStatus()).append(" | ").append(o.getOrderDate()).append("\n");
        return sb.toString();
    }
}
