package xyz.bolitao.simpsearchdemo.web;

import org.springframework.web.bind.annotation.GetMapping;

public class IndexController {
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }
}
