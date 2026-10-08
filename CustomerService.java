package com.customer.service;

import com.customer.dto.Requests;
import com.customer.exception.ApiException;
import com.customer.model.Customer;
import com.customer.repository.CustomerRepository;
import com.customer.repository.OrderRepository;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customers;
    private final OrderRepository orders;
    private final BCryptPasswordEncoder encoder;

    public CustomerService(CustomerRepository customers, OrderRepository orders, BCryptPasswordEncoder encoder) {
        this.customers = customers;
        this.orders = orders;
        this.encoder = encoder;
    }

    /** "+91 98765 43210" and "9876543210" become the same value. */
    public static String normalizePhone(String raw) {
        String d = raw == null ? "" : raw.replaceAll("[^0-9]", "");
        if (d.length() == 12 && d.startsWith("91")) d = d.substring(2);
        return d;
    }

    private static String cleanEmail(String e) { return e.trim().toLowerCase(); }

    private static String checkedPhone(String raw) {
        String p = normalizePhone(raw);
        if (!p.matches("\\d{10,15}")) throw new ApiException(400, "Phone number must have 10 to 15 digits.");
        return p;
    }

    public Customer get(@NonNull Long id) {
        return customers.findById(id).orElseThrow(() -> new ApiException(401, "Please sign in."));
    }

    public long count() { return customers.count(); }

    @Transactional
    public Customer register(Requests.Register r) {
        String email = cleanEmail(r.email());
        String phone = checkedPhone(r.phone());
        if (customers.existsByEmail(email)) throw new ApiException(409, "This email is already registered.");
        if (customers.existsByPhone(phone)) throw new ApiException(409, "This phone number is already registered.");
        Customer c = new Customer();
        c.setName(r.name().trim());
        c.setEmail(email);
        c.setPhone(phone);
        c.setPasswordHash(encoder.encode(r.password()));
        return customers.save(c);
    }

    /** identifier is an email (contains @) or a phone number. */
    public Customer login(String identifier, String password) {
        String id = identifier.trim();
        Optional<Customer> found = id.contains("@")
                ? customers.findByEmail(cleanEmail(id))
                : customers.findByPhone(normalizePhone(id));
        return found.filter(c -> encoder.matches(password, c.getPasswordHash()))
                .orElseThrow(() -> new ApiException(401, "Invalid email, phone number or password."));
    }

    @Transactional
    public Customer update(@NonNull Long id, Requests.ProfileUpdate r) {
        Customer c = get(id);
        String email = cleanEmail(r.email());
        String phone = checkedPhone(r.phone());
        if (customers.existsByEmailAndIdNot(email, id)) throw new ApiException(409, "That email is already used by another customer.");
        if (customers.existsByPhoneAndIdNot(phone, id)) throw new ApiException(409, "That phone number is already used by another customer.");
        c.setName(r.name().trim());
        c.setEmail(email);
        c.setPhone(phone);
        boolean hasAddress = r.address() != null && !r.address().isBlank();
        c.setAddress(hasAddress ? r.address().trim() : null);
        c.setLatitude(hasAddress ? r.latitude() : null);
        c.setLongitude(hasAddress ? r.longitude() : null);
        return customers.save(c);
    }

    @Transactional
    public void changePassword(@NonNull Long id, Requests.PasswordChange r) {
        Customer c = get(id);
        if (!encoder.matches(r.current(), c.getPasswordHash())) throw new ApiException(400, "Current password is incorrect.");
        c.setPasswordHash(encoder.encode(r.next()));
        customers.save(c);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        orders.deleteByCustomerId(id);
        customers.deleteById(id);
    }
}
