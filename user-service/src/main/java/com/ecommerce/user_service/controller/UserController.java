package com.ecommerce.user_service.controller;


import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.service.UserService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public String health(){
        System.out.println("Health Check for User Service is up and running");
        return "User health Check is up";
    }

    @PostMapping
    public User save(@RequestBody User user) {
        return userService.saveUser(user);
    }
}