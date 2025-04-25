package io.reflectoring.interceptor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LoginController {
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> creds) {
        return ResponseEntity.ok("Logged in as: " + creds.get("username"));
    }
}
