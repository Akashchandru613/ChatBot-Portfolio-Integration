package com.akash.portfoliochatbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AboutMeController {

    @GetMapping("/about-me")
    public String aboutMe() {
        return "Hi, I'm Akash — an aspiring AI Engineer with a background in web development!";
    }
}
