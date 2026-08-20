package com.harry.clio.controller;

import com.harry.clio.dto.user.AdminUserListResponse;
import com.harry.clio.dto.user.UserFilterRequest;
import com.harry.clio.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public String list(
            @Valid @ModelAttribute("filter") UserFilterRequest filter,
            BindingResult bindingResult,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable,
            Model model) {

        Page<AdminUserListResponse> users = bindingResult.hasErrors()
                ? Page.empty(pageable)
                : userService.getAllAdminUsers(filter, pageable);

        model.addAttribute("users", users);
        model.addAttribute("pageNumbers", createPageNumbers(users));
        return "html/users";
    }

    private List<Integer> createPageNumbers(Page<?> page) {
        if (page.getTotalPages() == 0) {
            return List.of();
        }

        int startPage = Math.max(0, page.getNumber() - 2);
        int endPage = Math.min(page.getTotalPages() - 1, startPage + 4);

        startPage = Math.max(0, endPage - 4);
        return IntStream.rangeClosed(startPage, endPage).boxed().toList();
    }
}
