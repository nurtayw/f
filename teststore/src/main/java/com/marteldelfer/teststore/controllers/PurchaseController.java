package com.marteldelfer.teststore.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.marteldelfer.teststore.repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.marteldelfer.teststore.models.Cart;
import com.marteldelfer.teststore.models.Product;
import com.marteldelfer.teststore.models.Purchase;
import com.marteldelfer.teststore.models.User;
import com.marteldelfer.teststore.repositories.CartRepository;
import com.marteldelfer.teststore.repositories.ProductRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

@Controller
public class PurchaseController {
    
    @Autowired
    PurchaseRepository purchaseRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    CartRepository cartRepo;

    @Autowired
    ProductRepository productRepo;

    @GetMapping("/purchase")
    public String purchase(
        Model model
    ) {
        //Finds current user id and cart
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        int userId = user.getId();
        Cart cart = cartRepo.findById(userId).get();

        if (!cart.getIndexList().isEmpty()) {

            int i = -1;
            List<Integer> quantityList = cart.getQuantityList();
            for (int productIndex : cart.getIndexList()) {

                i++;
                Product product = productRepo.findById(productIndex).get();

                if (product.getQuantity() < quantityList.get(i)) {
                    
                    model.addAttribute("notEnough", true);
                    model.addAttribute("product", product);
                    return "cart.html";
                }
            }

            i = -1;
            for (Integer productIndex : cart.getIndexList()) {
                
                i++;
                Product product = productRepo.findById(productIndex).get();
                product.setQuantity(product.getQuantity() - quantityList.get(i));
                productRepo.save(product);
            }

            //Creating  and saving purchase
            Purchase purchase = new Purchase();
            purchase.setProductList(cart.getIndexList());
            purchase.setProductQuantity(cart.getQuantityList());
            purchase.setCreatedAt(new Date());
            purchase.setUserId(userId);
            purchase.setSent(false);
            purchaseRepo.save(purchase);

            //Clearing cart
            List<Integer> newList = new ArrayList<>();
            cart.setIndexList(newList);
            cart.setQuantityList(newList);
            cartRepo.save(cart);

            //Adds purchase to model
            List<Product> products = new ArrayList<>();
            for (int productIndex : purchase.getProductList()) {
                products.add(productRepo.findById(productIndex).get());
            }
            model.addAttribute("products", products);
            model.addAttribute("quantityList", quantityList);
            model.addAttribute("success", true);

        } else {
            model.addAttribute("failed", true);
            return "cart.html";
        }
        
        
        return "purchase-completed.html";
    }

    @GetMapping("/order-history")
    public String showOrderHistory(
        Model model
    ) {
        //Finds current user id and cart
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        int userId = user.getId();

        List<Purchase> orders = purchaseRepo.findByUserId(userId);
        ProductRepository products = productRepo;

        model.addAttribute("orders", orders);
        model.addAttribute("products", products);
        
        return "order-history.html";
    }
}