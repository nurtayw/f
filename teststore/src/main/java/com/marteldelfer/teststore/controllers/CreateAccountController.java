package com.marteldelfer.teststore.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.marteldelfer.teststore.models.Cart;
import com.marteldelfer.teststore.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.marteldelfer.teststore.models.User;
import com.marteldelfer.teststore.models.UserDto;
import com.marteldelfer.teststore.repositories.RoleRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

import jakarta.validation.Valid;

@Controller
public class CreateAccountController {
    
    @Autowired
    private UserRepository repo;
    
    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/create-account")
    public String showCreateAccountPage(Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute(userDto);
        model.addAttribute("success", false);

        return "create-account.html";
    }

    @PostMapping("/create-account")
    public String createAccount(
        Model model,
        @Valid @ModelAttribute UserDto userDto,
        BindingResult result
    ) {

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.addError(new FieldError(
                "userDto",
                "confirmPassword",
                "Password and confirm password do not match")
                );
        }

        User user = repo.findByEmail(userDto.getEmail());
        if (user != null) {
            result.addError(new FieldError("userDto", "email",
                "Email adress is already in use"));
        }

        if (result.hasErrors()) {
            return "/create-account";
        }

        try {
            
            var bCryptEncoder = new BCryptPasswordEncoder();

            User newUser = new User();
            newUser.setFirstName(userDto.getFirstName());
            newUser.setLastName(userDto.getLastName());
            newUser.setEmail(userDto.getEmail());
            newUser.setCreatedAt(new Date());
            newUser.setPassword(bCryptEncoder.encode(userDto.getPassword()));
            newUser.setEnabled(true);
            newUser.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));

            repo.save(newUser);

            Cart cart = new Cart();
            List<Integer> list = new ArrayList<>();
            cart.setIndexList(list);
            cart.setQuantityList(list);
            cartRepo.save(cart);

            model.addAttribute("userDto", new UserDto());
            model.addAttribute("success", true);

        } catch (Exception ex) {
            result.addError(new FieldError("registerDto", "firstName",
             ex.getMessage()));
        }

        return "/create-account";
    }
}
