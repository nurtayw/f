package com.marteldelfer.teststore.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.marteldelfer.teststore.models.Privilege;
import com.marteldelfer.teststore.models.Role;
import com.marteldelfer.teststore.repositories.PrivilegeRepository;
import com.marteldelfer.teststore.repositories.RoleRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

@Service("userDetailsService")
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    PrivilegeRepository privilegeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        com.marteldelfer.teststore.models.User user = repo.findByEmail(email);

        if (user == null) {
            return new org.springframework.security.core.userdetails.User(
                " ", " ", true, true, true, true,
                getAuthorities(Arrays.asList(
                    roleRepository.findByName("ROLE_USER")
                )));
        }

        var springUser = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), user.getEnabled(), true, true, true,
                getAuthorities(user.getRoles()));
        return springUser;
    } 
    private Collection<? extends GrantedAuthority> getAuthorities(
        Collection<Role> roles) {

            return getGrantedAuthorities(getPrivileges(roles));
        }

    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }
}