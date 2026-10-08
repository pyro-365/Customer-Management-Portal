package com.customer.controller;

import com.customer.dto.Requests;
import com.customer.dto.Views.CustomerView;
import com.customer.security.SessionUser;
import com.customer.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CustomerService customers;

    public ProfileController(CustomerService customers) { this.customers = customers; }

    @PutMapping
    public CustomerView update(@Valid @RequestBody Requests.ProfileUpdate r, HttpSession s) {
        return CustomerView.of(customers.update(SessionUser.id(s), r));
    }

    @PutMapping("/password")
    public void changePassword(@Valid @RequestBody Requests.PasswordChange r, HttpSession s) {
        customers.changePassword(SessionUser.id(s), r);
    }

    @DeleteMapping
    public void delete(HttpSession s) {
        customers.delete(SessionUser.id(s));
        s.invalidate();
    }
}
