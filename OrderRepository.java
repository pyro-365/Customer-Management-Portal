package com.customer.repository;

import com.customer.model.CustomerOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomerIdOrderByIdDesc(Long customerId);
    List<CustomerOrder> findTop5ByCustomerIdOrderByIdDesc(Long customerId);
    long countByCustomerId(Long customerId);
    void deleteByCustomerId(Long customerId);
}
