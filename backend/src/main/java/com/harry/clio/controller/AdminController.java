package com.harry.clio.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class AdminController {
    @GetMapping("/login")
    public String login() {
        return "html/login";
    }

    @GetMapping
    public String index() {
        return "html/index";
    }
}
