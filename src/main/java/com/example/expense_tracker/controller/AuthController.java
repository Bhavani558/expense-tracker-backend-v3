package com.example.expense_tracker.controller;

import com.example.expense_tracker.dto.RegisterRequest;
import com.example.expense_tracker.entity.User;
import com.example.expense_tracker.repository.UserRepository;

import com.example.expense_tracker.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.expense_tracker.dto.LoginRequest;
import com.example.expense_tracker.security.JwtService;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists";
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        userRepository.save(user);

        return "User Registered Successfully";
    }

    @PostMapping("/login")
    public String login(
            @RequestBody LoginRequest request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElse(null);

        if (user == null) {
            return "Invalid Email";
        }

        boolean matches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!matches) {
            return "Invalid Password";
        }

        return jwtService.generateToken(
                user.getEmail()
        );
    }
}
