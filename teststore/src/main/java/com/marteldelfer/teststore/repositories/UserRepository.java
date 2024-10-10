package com.marteldelfer.teststore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marteldelfer.teststore.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findByEmail(String email);
}
