package com.marteldelfer.teststore.controllers;

import java.util.List;

import com.marteldelfer.teststore.models.Purchase;
import com.marteldelfer.teststore.repositories.CartRepository;
import com.marteldelfer.teststore.repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.marteldelfer.teststore.models.User;
import com.marteldelfer.teststore.models.UserDto;
import com.marteldelfer.teststore.repositories.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    @Autowired
    UserRepository userRepo;

    @Autowired
    PurchaseRepository purchaseRepo;

    @Autowired
    CartRepository cartRepo;

    @GetMapping({"/",""})
    public String showProfile(Model model) {

        //Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);

        model.addAttribute("user", user);
        model.addAttribute("success", false);

        return "profile-page.html";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {

        //Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);

        String password = "password";

        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(password);

        model.addAttribute("userDto", userDto);
        
        return "edit-profile.html";
    }

    @PostMapping("/edit")
    public String postEditProfile(
        Model model,
        @Valid @ModelAttribute UserDto userDto,
        BindingResult result
    ) {
        //Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);

        if (result.hasErrors()) {
            System.out.println("Exception: " + result.getFieldErrors().toString());
            return "edit-profile.html";
        }

        try {
            
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            
            userRepo.save(user);

            model.addAttribute("success", true);

        } catch (Exception ex) {
            result.addError(new FieldError("userDto","firstName", ex.getMessage()));
        }

        model.addAttribute("user", user);

        return "profile-page.html";
    }

    @GetMapping("/delete")
    public String deleteUser() {
        //Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        Integer userIndex = user.getId();

        try {
            userRepo.delete(user);
            cartRepo.delete(cartRepo.findById(userIndex).get());
            List<Purchase> purchases = purchaseRepo.findByUserId(userIndex);

            for (Purchase purchase : purchases) {
                purchaseRepo.delete(purchase);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        
        return "redirect:/";
    }
}
