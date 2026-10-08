package com.customer.controller;

import com.customer.dto.Requests;
import com.customer.dto.Views;
import com.customer.security.SessionUser;
import com.customer.service.AssistantService;
import com.customer.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistant;
    private final CustomerService customers;

    public AssistantController(AssistantService assistant, CustomerService customers) {
        this.assistant = assistant;
        this.customers = customers;
    }

    @PostMapping
    public Views.ChatReply chat(@Valid @RequestBody Requests.Chat r, HttpSession s) {
        return new Views.ChatReply(assistant.reply(customers.get(SessionUser.id(s)), r));
    }
}
