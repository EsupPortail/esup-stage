package fr.dauphine.estage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/helloworld")
    public String testApp() {
        return "Hello world";
    }

}
