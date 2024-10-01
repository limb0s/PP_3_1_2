package ru.kata.spring.boot_security.demo.controllers;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.services.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminController(final UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping()
    public String admin(Model model) {
        model.addAttribute("users", userService.getUsers());
        return "admin";
    }

    @GetMapping("/addUser")
    public String newUser(@ModelAttribute("user") User user) {
        Set<Role> roles = roleRepository.findAll().stream().collect(Collectors.toSet());
        user.setRoles(roles);
        return "newUser";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("user") @Valid User user,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "newUser";
        }
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model) {
        Set<Role> roles = roleRepository.findAll().stream().collect(Collectors.toSet());
        if(userService.getUser(id) == null){
            System.out.println("user not found");
        }
        User user = userService.getUser(id);
        user.setRoles(roles);
        user.setCurrentPassword(user.getPassword());
        model.addAttribute("allRoles",roles);
        model.addAttribute("user", user);
        return "updateUser";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") @Valid User user,
                             BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "updateUser";
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        } else {
          user.setPassword(user.getCurrentPassword());
        }
        userService.updateUser(user.getId(), user);
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id, Principal principal) {
        if (principal.getName().equals(userService.getUser(id).getUsername())) {
            userService.deleteUser(id);
            return "redirect:/logout";
        }
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
