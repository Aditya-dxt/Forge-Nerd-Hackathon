package com.goalflow.backend.controller;

import com.goalflow.backend.model.User;
import com.goalflow.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String name = body.get("name");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password are required"));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "An account with this email already exists"));
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password); // plaintext for hackathon demo only
        user.setName(name != null ? name : "User");

        User saved = userRepository.save(user);
        String token = generateToken(saved);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("token", token, "userId", saved.getId(), "name", saved.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email and password are required"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }

        User user = userOpt.get();
        String token = generateToken(user);

        return ResponseEntity.ok(Map.of("token", token, "userId", user.getId(), "name", user.getName()));
    }

    /**
     * Generate a simple JWT-like token for demo purposes.
     * Format: base64(header).base64(payload).stub
     * The frontend decodes the middle segment to read user info.
     */
    private String generateToken(User user) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());

        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"email\":\"%s\",\"name\":\"%s\",\"iat\":%d}",
                user.getId(), user.getEmail(), user.getName(), System.currentTimeMillis() / 1000
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());

        return header + "." + payload + ".demo-signature";
    }
}
