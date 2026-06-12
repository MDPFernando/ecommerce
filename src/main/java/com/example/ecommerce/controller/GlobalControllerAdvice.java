package com.example.ecommerce.controller;

import com.example.ecommerce.model.Settings;
import com.example.ecommerce.repository.SettingsRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private SettingsRepository settingsRepository;

    @ModelAttribute("adminWhatsapp")
    public String getAdminWhatsapp() {
        return settingsRepository.findById("admin_whatsapp")
                .map(Settings::getSettingValue)
                .orElse("94769414472");
    }
}
