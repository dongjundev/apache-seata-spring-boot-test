package com.example.inventory.repository;

import com.example.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);

    @Modifying
    @Query(value = "UPDATE inventory SET quantity = (SELECT quantity FROM inventory WHERE product_id = :productId) - :deductQuantity WHERE product_id = :productId", nativeQuery = true)
    int deductBySubqueryUpdate(@Param("productId") String productId, @Param("deductQuantity") int deductQuantity);
}
