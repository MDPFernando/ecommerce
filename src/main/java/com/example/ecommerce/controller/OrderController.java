package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createOrder(@RequestBody Order order) {
        Order savedOrder = orderRepository.save(order);
        return Map.of("success", true, "orderId", savedOrder.getId());
    }

    @GetMapping("/track")
    public String viewTrackPage(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            try {
                Long id = Long.parseLong(query);
                orderRepository.findById(id).ifPresent(o -> model.addAttribute("order", o));
            } catch (NumberFormatException e) {
                List<Order> orders = orderRepository.findByCustomerPhoneOrCustomerWhatsapp(query, query);
                if (!orders.isEmpty()) {
                    model.addAttribute("orders", orders);
                }
            }
        }
        return "track-order";
    }
}
