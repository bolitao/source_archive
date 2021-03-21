package xyz.bolitao.springsecuritydemo.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("test")
    public String hello() {
        return "site: bolitao.xyz";
    }

    @GetMapping(value = "/admin/test")
    public String admin() {
        return "admin test";
    }

    @GetMapping(value = "/user/test")
    public String user() {
        return "user test";
    }

    @GetMapping(value = "success")
    public String success() {
        return "success";
    }
}
