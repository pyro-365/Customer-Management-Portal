package com.customer.dto;

import com.customer.model.Customer;
import com.customer.model.CustomerOrder;
import java.math.BigDecimal;
import java.util.List;

/** Everything the server sends back (never includes the password hash). */
public final class Views {
    private Views() {}

    public record CustomerView(Long id, String name, String email, String phone,
                               String address, Double latitude, Double longitude) {
        public static CustomerView of(Customer c) {
            return new CustomerView(c.getId(), c.getName(), c.getEmail(), c.getPhone(),
                    c.getAddress(), c.getLatitude(), c.getLongitude());
        }
    }

    public record Dashboard(long totalCustomers, long myOrders, BigDecimal totalSpent, List<CustomerOrder> recentOrders) {}

    public record GeoResult(String formattedAddress, double lat, double lng, String accuracy) {}

    public record RouteResult(double distanceKm, double durationMinutes, Object geometry, String summary) {}

    public record ChatReply(String reply) {}
}
