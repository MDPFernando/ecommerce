package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.example.ecommerce.repository.ReviewRepository reviewRepository;

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            Double avg = reviewRepository.getAverageRatingForProduct(p.getId());
            Integer count = reviewRepository.getReviewCountForProduct(p.getId());
            p.setAverageRating(avg != null ? avg : 0.0);
            p.setReviewCount(count != null ? count : 0);
        }
        return products;
    }

    public Product getProductById(Long id) {
        Product p = productRepository.findById(id).orElse(null);
        if (p != null) {
            Double avg = reviewRepository.getAverageRatingForProduct(p.getId());
            Integer count = reviewRepository.getReviewCountForProduct(p.getId());
            p.setAverageRating(avg != null ? avg : 0.0);
            p.setReviewCount(count != null ? count : 0);
        }
        return p;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
