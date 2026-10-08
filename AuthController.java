package com.customer.controller;

import com.customer.dto.Requests;
import com.customer.dto.Views.CustomerView;
import com.customer.model.Customer;
import com.customer.security.SessionUser;
import com.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerService customers;

    public AuthController(CustomerService customers) { this.customers = customers; }

    @PostMapping("/register")
    public CustomerView register(@Valid @RequestBody Requests.Register r) {
        return CustomerView.of(customers.register(r));
    }

    @PostMapping("/login")
    public CustomerView login(@Valid @RequestBody Requests.Login r, HttpServletRequest req) {
        Customer c = customers.login(r.identifier(), r.password());
        req.getSession(true);
        req.changeSessionId();                       // new session id after login
        req.getSession().setAttribute(SessionUser.KEY, c.getId());
        return CustomerView.of(c);
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) { session.invalidate(); }

    @GetMapping("/me")
    public CustomerView me(HttpSession session) {
        return CustomerView.of(customers.get(SessionUser.id(session)));
    }
}
