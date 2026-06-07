package com.disciplica.server.support;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicStatusController {
    private static final String BUILD_MARKER = "security-ant-public-auth";

    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of(
                "service", "disciplica-api",
                "status", "ok",
                "build", BUILD_MARKER
        );
    }
}
