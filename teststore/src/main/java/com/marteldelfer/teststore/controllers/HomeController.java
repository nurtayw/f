package com.marteldelfer.teststore.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.marteldelfer.teststore.models.Product;
import com.marteldelfer.teststore.repositories.ProductRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

@Controller
public class HomeController {

    @Autowired
    ProductRepository repo;
    
    @GetMapping("")
    public String showHomePage(Model model) {
        List<Product> allProducts = repo.findAll(Sort.by("id"));
        List<List<Product>> products = new ArrayList<>();

        int i = 0;
        List<Product> innerList = new ArrayList<>();
        for (Product product : allProducts) {

            innerList.add(product);
            i++;

            if (i == 5) {
                i = 0;
                List<Product> newProducts = new ArrayList<>();
                newProducts.addAll(innerList);
                products.add(newProducts);
                innerList.clear();
            }
        }
        if (!innerList.isEmpty()) {
            products.add(innerList);
        }
        model.addAttribute("products", products);
        return "index.html";
    }

    @GetMapping("/show")
    public String showProduct(
        Model model,
        @RequestParam int id
        ) {
        Product product = repo.findById(id).get();
        model.addAttribute("product", product);
        model.addAttribute("success", false);
        return "show-product.html";
    }

    @GetMapping("/search")
    public String search(Model model, @RequestParam String keyword) {
        System.out.println("Searching with " + keyword);
        List<Product> allProducts = repo.searchProducts(keyword);
        List<List<Product>> products = new ArrayList<>();

        int i = 0;
        List<Product> innerList = new ArrayList<>();
        for (Product product : allProducts) {

            innerList.add(product);
            i++;

            if (i == 5) {
                i = 0;
                List<Product> newProducts = new ArrayList<>();
                newProducts.addAll(innerList);
                products.add(newProducts);
                innerList.clear();
            }
        }
        if (!innerList.isEmpty()) {
            products.add(innerList);
        }
        model.addAttribute("products", products);
        return "search.html";
    }
}
