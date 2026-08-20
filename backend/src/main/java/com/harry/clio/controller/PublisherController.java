package com.harry.clio.controller;

import com.harry.clio.dto.publisher.AdminPublisherDto;
import com.harry.clio.dto.publisher.PublisherForm;
import com.harry.clio.service.PublisherService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class PublisherController {
    private final PublisherService publisherService;

    @GetMapping("/publishers")
    public String list(Model model) {
        model.addAttribute("publishers", publisherService.getAllPublishers());
        return "html/publishers";
    }

    @GetMapping("/publishers/create")
    public String createView(Model model) {
        model.addAttribute("publisherForm", new PublisherForm());
        model.addAttribute("availableUsers", publisherService.getUserOptions());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/publishers/create");
        return "html/publisher-form";
    }

    @PostMapping("/publishers/create")
    public String create(
            @Valid @ModelAttribute("publisherForm") PublisherForm publisherForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("availableUsers", publisherService.getUserOptions());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/publishers/create");
            return "html/publisher-form";
        }
        publisherService.createPublisher(publisherForm);
        return "redirect:/publishers";
    }

    @GetMapping("/publishers/{userId}")
    public String detail(@PathVariable int userId, Model model) {
        model.addAttribute("publisher", publisherService.getPublisherAdmin(userId));
        return "html/publisher-details";
    }

    @GetMapping("/publishers/{userId}/edit")
    public String editView(@PathVariable int userId, Model model) {
        AdminPublisherDto publisher = publisherService.getPublisherAdmin(userId);
        PublisherForm form = new PublisherForm();
        form.setUserId(publisher.userId());
        form.setBankAccountNumber(publisher.bankAccountNumber());

        model.addAttribute("publisherForm", form);
        model.addAttribute("selectedPublisher", publisher);
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/publishers/" + userId + "/edit");
        return "html/publisher-form";
    }

    @PostMapping("/publishers/{userId}/edit")
    public String edit(
            @PathVariable int userId,
            @Valid @ModelAttribute("publisherForm") PublisherForm publisherForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("selectedPublisher", publisherService.getPublisherAdmin(userId));

            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/publishers/" + userId + "/edit");

            return "html/publisher-form";
        }
        publisherService.updatePublisher(userId, publisherForm);
        return "redirect:/publishers/" + userId;
    }
}
