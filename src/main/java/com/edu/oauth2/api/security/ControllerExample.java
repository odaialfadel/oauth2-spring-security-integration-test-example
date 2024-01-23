package com.edu.oauth2.api.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControllerExample {

    @GetMapping("/api/example")
    public String getSecureResponse() {
        return "Received secure response.";
    }

    @GetMapping("/example")
    public String getUnsecureResponse() {
        return "Received unsecure response.";
    }
}
