package com.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index(){
        return "Server is running";
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "World") String name){
        return "Hello +" + name +"!";
    }
}
