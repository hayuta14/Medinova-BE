package com.project.medinova.repository;

import com.project.medinova.entity.PharmacyOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PharmacyOrderItemRepository extends JpaRepository<PharmacyOrderItem, Long> {
    List<PharmacyOrderItem> findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}

