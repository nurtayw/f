package com.marteldelfer.teststore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marteldelfer.teststore.models.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    public Role findByName(String name);
}
