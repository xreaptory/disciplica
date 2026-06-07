package com.disciplica.server.support;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicStatusController {

    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of(
                "service", "disciplica-api",
                "status", "ok"
        );
    }
}
