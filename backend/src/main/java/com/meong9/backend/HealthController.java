package com.meong9.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/test")
    public String healthCheck() {
        return "OK";
    }
}
