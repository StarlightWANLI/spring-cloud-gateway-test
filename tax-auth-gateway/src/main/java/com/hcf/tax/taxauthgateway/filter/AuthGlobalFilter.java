package com.hcf.tax.taxauthgateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 不用进行配置，自动生效
 * @program: tax-auth-gateway
 * @description: 全局权限过滤器
 * @author: wanli
 * @create: 2019-05-31 11:18
 **/
//@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Welcome to AuthGlobalFilter.");
        ServerHttpRequest request = exchange.getRequest();

        //模拟token验证
       // String sign = request.getHeaders().get("sign").get(0);
        String sign = request.getHeaders().getFirst("sign");
        String token = "1234";
        if(token.equals(sign)) {
            //通过认证
            return chain.filter(exchange);
        }
        //认证失败，返回没有权限
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
