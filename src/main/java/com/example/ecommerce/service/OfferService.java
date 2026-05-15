package com.example.ecommerce.service;

import com.example.ecommerce.model.Offer;
import com.example.ecommerce.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.model.Product;

import java.util.List;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;
    
    @Autowired
    private ProductRepository productRepository;

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Offer getOfferById(Long id) {
        return offerRepository.findById(id).orElse(null);
    }

    public Offer getActiveOffer() {
        List<Offer> allOffers = offerRepository.findAll();
        if (allOffers.isEmpty()) return null;
        return allOffers.get(0); // For simplicity, return the first offer
    }

    @Transactional
    public void saveOffer(Offer offer) {
        // Secure Transaction Protocol: Load managed product within the same transaction bounds
        if (offer.getProduct() != null && offer.getProduct().getId() != null) {
            Product managedProduct = productRepository.findById(offer.getProduct().getId()).orElse(null);
            offer.setProduct(managedProduct);
        } else {
            offer.setProduct(null);
        }
        offerRepository.save(offer);
    }

    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }
}
