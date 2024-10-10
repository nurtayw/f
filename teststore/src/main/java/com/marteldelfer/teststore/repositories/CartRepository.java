package com.marteldelfer.teststore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marteldelfer.teststore.models.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

}
