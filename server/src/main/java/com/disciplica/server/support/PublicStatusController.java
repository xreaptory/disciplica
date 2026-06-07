package com.disciplica.server.support;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublicStatusController {
    private static final String BUILD_MARKER = "ant-ignoring-v2";

    @GetMapping("/")
    public Map<String, String> index() {
        return status();
    }

    @GetMapping({"/status", "/healthz"})
    public Map<String, String> status() {
        return Map.of(
                "service", "disciplica-api-now5",
                "status", "ok",
                "build", BUILD_MARKER
        );
    }
}
