package com.jobagent.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
        "/login",
        "/register",
        "/dashboard",
        "/jobs",
        "/jobs/**",
        "/applications",
        "/applications/**",
        "/resume",
        "/profile",
        "/emails",
        "/notifications",
        "/settings",
        "/integrations"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
