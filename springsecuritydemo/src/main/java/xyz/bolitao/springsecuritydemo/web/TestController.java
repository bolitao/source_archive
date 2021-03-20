package xyz.bolitao.springsecuritydemo.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("test")
    public String hello() {
        return "site: bolitao.xyz";
    }

    @GetMapping(value = "success")
    public String success() {
        return "success";
    }
}
