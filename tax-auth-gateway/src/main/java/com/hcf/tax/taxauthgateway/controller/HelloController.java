package com.hcf.tax.taxauthgateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @program: tax-auth-gateway
 * @description: hello控制层
 * @author: wanli
 * @create: 2019-05-30 14:50
 **/
@RestController
public class HelloController {


    @GetMapping("/hello")
    public String hello(){
        return  "hello world";
    }

    @GetMapping("/auth")
    public String auth(){
        return  "hello world";
    }


    @RequestMapping("/hystrixfallback")
    public Mono<String> fallback() {
        return Mono.just("This is a fallback");
    }

}
