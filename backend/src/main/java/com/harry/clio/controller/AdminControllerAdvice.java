package com.harry.clio.controller;

import com.harry.clio.dto.CustomUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdminControllerAdvice {
    @ModelAttribute
    public void addCurrentAdmin(Model model, @AuthenticationPrincipal CustomUser principal) {
        if (principal != null) {
            model.addAttribute("currentAdmin", principal);
        }
    }
}
