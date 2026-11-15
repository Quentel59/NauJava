package ru.quentel.naujava.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.quentel.naujava.service.AppUserService;

@Controller
public class RegistrationController {

    private final AppUserService appUserService;

    @Autowired
    public RegistrationController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/registration")
    public String registrationForm() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String email,
                           Model model) {
        try {
            appUserService.registerUser(username, password, email);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "registration";
        }
    }
}