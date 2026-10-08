package com.customer.repository;

import com.customer.model.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p where "
         + "(lower(p.name) like lower(concat('%', :q, '%')) or lower(coalesce(p.category, '')) like lower(concat('%', :q, '%'))) "
         + "and (:category = '' or p.category = :category)")
    Page<Product> search(@Param("q") String q, @Param("category") String category, Pageable pageable);

    @Query("select distinct p.category from Product p where p.category is not null order by p.category")
    List<String> categories();

    List<Product> findTop10ByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);

    /** Locks the row while an order is placed so two buyers cannot take the last item. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(@Param("id") Long id);
}
