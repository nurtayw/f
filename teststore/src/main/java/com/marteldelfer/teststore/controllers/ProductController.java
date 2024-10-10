
package com.marteldelfer.teststore.controllers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.query.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.marteldelfer.teststore.models.Product;
import com.marteldelfer.teststore.models.ProductDto;
import com.marteldelfer.teststore.models.Purchase;
import com.marteldelfer.teststore.models.User;
import com.marteldelfer.teststore.repositories.ProductRepository;
import com.marteldelfer.teststore.repositories.PurchaseRepository;
import com.marteldelfer.teststore.repositories.UserRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/crud")
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @GetMapping("")
    public String showCrudPage(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "product-crud.html";
    }

    @GetMapping("/create-product")
    public String showCreateProductPage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        model.addAttribute("success", false);
        return "create-product.html";
    }

    @PostMapping("/create-product")
    public String postCreateProduct(
            Model model,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
    ) {

        if (productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError(
                    "productDto",
                    "imageFile",
                    "Image is required"));
        }

        if (result.hasErrors()) {
            return "create-product.html";
        }

        //save image
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            System.out.println("Exeption" + e.getMessage());
        }

        Product newProduct = new Product();

        newProduct.setName(productDto.getName());
        newProduct.setBrand(productDto.getBrand());
        newProduct.setCategory(productDto.getCategory());
        newProduct.setDescription(productDto.getDescription());
        newProduct.setPrice(productDto.getPrice());
        newProduct.setQuantity(productDto.getQuantity());
        newProduct.setCreatedAt(createdAt);
        newProduct.setImageName(storageFileName);

        repo.save(newProduct);
        model.addAttribute("success", true);

        return "create-product.html";
    }

    @GetMapping("/edit")
    public String editProduct(
            Model model,
            @RequestParam int id
    ) {

        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());
            productDto.setQuantity(product.getQuantity());

            model.addAttribute("productDto", productDto);

        } catch (Exception ex){
            System.out.println("Exception: " + ex.getMessage());
            return "redirect/crud";
        }
        return "edit-product.html";
    }

    @PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
    ) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "edit-product.html";
            }

            if (!productDto.getImageFile().isEmpty()) {
                String uploadDir = "/public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageName());

                try {
                    Files.delete(oldImagePath);

                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageName(storageFileName);
            }
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setQuantity(productDto.getQuantity());

            repo.save(product);

        }
        catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/crud";
    }

    @GetMapping("/delete")
    public String deleteProduct(
            @RequestParam int id
    ) {

        try {
            Product product = repo.findById(id).get();
            Path imagePath = Paths.get("/public/images" + product.getImageName());

            try {
                Files.delete(imagePath);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex);
            }

            repo.delete(product);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return "redirect:/crud";
    }
    @GetMapping("/all-orders")
    public String showAllOrders(Model model) {

        ProductRepository products = repo;
        List<Purchase> purchases = purchaseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        UserRepository users = userRepository;

        List<Double> totals = new ArrayList<>();
        for (Purchase purchase : purchases) {
            for (int i = 0; i < purchase.getProductList().size(); i++) {
                Product product = products.findById(purchase.getProductList().get(i)).get();
                totals.add(product.getPrice() * purchase.getProductQuantity().get(i));
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("purchases", purchases);
        model.addAttribute("users", users);
        model.addAttribute("totals", totals);

        return "all-orders.html";
    }

    @GetMapping("/send-product")
    public String sendProduct(
            Model model,
            @RequestParam int id
    ) {
        Purchase purchase = purchaseRepository.findById(id).get();
        purchase.setSent(true);
        purchaseRepository.save(purchase);

        return "redirect:/crud/all-orders";
    }
}
