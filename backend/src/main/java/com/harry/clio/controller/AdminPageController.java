package com.harry.clio.controller;

import com.harry.clio.exception.BadRequestException;
import com.harry.clio.service.StatService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AdminPageController {
    private final StatService statService;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "MONTH") String time,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) Integer year,
            Model model) {
        String normalizedTime = time.strip().toUpperCase(Locale.ROOT);

        LocalDate today = LocalDate.now(ZoneId.of(zoneId));
        int selectedYear = year == null ? today.getYear() : year;

        int defaultPeriod = 1;

        if (selectedYear == today.getYear()) {
            defaultPeriod = normalizedTime.equals("MONTH")
                    ? today.getMonthValue()
                    : (today.getMonthValue() - 1) / 3 + 1;
        }

        int selectedPeriod = period == null ? defaultPeriod : period;

        model.addAttribute(
                "stats",
                statService.getPlatformRevenue(normalizedTime, selectedPeriod, selectedYear));

        return "index";
    }

    @ExceptionHandler(BadRequestException.class)
    public String invalidFilter(
            BadRequestException exception, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("error", exception.getMessage());
        return "redirect:/";
    }
}
