package com.marteldelfer.teststore.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.marteldelfer.teststore.models.Cart;
import com.marteldelfer.teststore.models.Product;
import com.marteldelfer.teststore.models.User;
import com.marteldelfer.teststore.repositories.CartRepository;
import com.marteldelfer.teststore.repositories.ProductRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

@Controller
public class CartController {

    @Autowired
    ProductRepository productRepo;

    @Autowired
    CartRepository cartRepo;

    @Autowired
    UserRepository userRepo;
    
    @GetMapping("/add-to-cart")
    public String addToCart(
        Model model,
        @RequestParam int id
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {

            String email = authentication.getName();
            User user = userRepo.findByEmail(email);
            int userId = user.getId();
            Cart cart = cartRepo.findById(userId).get();

            if (!cart.getIndexList().contains(id)) {
                cart.getIndexList().add(id);
                cart.getQuantityList().add(1);
                cartRepo.save(cart);
            } else {
                int quantityIndex = cart.getIndexList().indexOf(id);
                int quantity = cart.getQuantityList().get(quantityIndex);
                cart.getQuantityList().set(quantityIndex,
                (quantity + 1));

                cartRepo.save(cart);
            }

            Product product = productRepo.findById(id).get();

            model.addAttribute("product", product);
            model.addAttribute("success", true);
            return "show-product.html";

        } else {
            return "/login";
        } 
    }
    @GetMapping("/cart")
    public String cart(
        Model model
    ) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        int userId = user.getId();
        Cart cart = cartRepo.findById(userId).get();

        List<Product> products = new ArrayList<Product>();
        List<Integer> listIndex = cart.getIndexList();
        List<Integer> listQuantity = cart.getQuantityList();
        listIndex.forEach(index -> products.add(productRepo.findById(index).get()));

        double totalCost = 0;
        int i = -1;
        for (Product product : products) {
            i += 1;
            totalCost += product.getPrice() * listQuantity.get(i);
        }
        
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("products", products);
        model.addAttribute("listQuantity", listQuantity);
        return "cart.html";
    }

    @GetMapping("/remove")
    public String removeFromCart(
        Model model,
        @RequestParam int id
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        int userId = user.getId();
        Cart cart = cartRepo.findById(userId).get();
        
        List<Integer> products = cart.getIndexList();
        List<Integer> listQuantity = cart.getQuantityList();

        int index = products.indexOf(id);
        products.remove(index);
        listQuantity.remove(index);

        cart.setIndexList(products);
        cart.setQuantityList(listQuantity);

        cartRepo.save(cart);

        return "redirect:/cart";
    }
}