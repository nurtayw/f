package com.marteldelfer.teststore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marteldelfer.teststore.models.Privilege;

public interface PrivilegeRepository extends JpaRepository<Privilege, Integer> {
    
    public Privilege findByName(String name);

}
