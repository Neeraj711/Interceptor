package io.reflectoring.interceptor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from API!");
    }

    @PostMapping("/data")
    public ResponseEntity<String> postData(@RequestBody String body) {
        return ResponseEntity.ok("Received data: " + body);
    }


    @GetMapping("/error")
    public ResponseEntity<String> throwError() {
        throw new RuntimeException("Test exception");
    }
}

