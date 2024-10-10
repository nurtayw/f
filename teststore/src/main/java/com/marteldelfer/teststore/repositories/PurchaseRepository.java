package com.marteldelfer.teststore.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marteldelfer.teststore.models.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer>{

    public List<Purchase> findByUserId(int userId);
}
