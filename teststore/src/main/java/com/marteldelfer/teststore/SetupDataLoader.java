package com.marteldelfer.teststore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.marteldelfer.teststore.models.Cart;
import com.marteldelfer.teststore.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.marteldelfer.teststore.models.Privilege;
import com.marteldelfer.teststore.models.Role;
import com.marteldelfer.teststore.models.User;
import com.marteldelfer.teststore.repositories.PrivilegeRepository;
import com.marteldelfer.teststore.repositories.RoleRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    
    boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private CartRepository cartRepository;

    @Transactional
    public Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege();
            privilege.setName(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    public Role createRoleIfNotFound(
        String name, Collection<Privilege> privileges) {
        
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;  
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            return;
        }
        User newuser = userRepository.findByEmail("firstAdmin@gmail.com");
        if (newuser == null) {
            Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");

            Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

            List<Privilege> adminPrivileges = Arrays.asList(
                readPrivilege, writePrivilege);
            createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
            createRoleIfNotFound("ROLE_USER", Arrays.asList(readPrivilege));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            var encoder = new BCryptPasswordEncoder();

            User user = new User();
            user.setFirstName("first");
            user.setLastName("Admin");
            user.setEmail("firstAdmin@gmail.com");
            user.setPassword(encoder.encode("firstAdmin"));
            user.setCreatedAt(new Date());
            user.setRoles(Arrays.asList(adminRole));
            user.setEnabled(true);
            userRepository.save(user);

            Cart cart = new Cart();
            List<Integer> list = new ArrayList<>();
            cart.setIndexList(list);
            cart.setQuantityList(list);
            cartRepository.save(cart);

            alreadySetup = true;
        }
    }
}
