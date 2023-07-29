package com.integralblue.demo.jumpstart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping(value = "/")
    public String index() {
        return "forward:index.html";
    }
}
