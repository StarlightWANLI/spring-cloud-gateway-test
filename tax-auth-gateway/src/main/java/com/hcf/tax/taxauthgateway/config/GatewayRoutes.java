package com.hcf.tax.taxauthgateway.config;

import com.hcf.tax.taxauthgateway.filter.AuthGatewayFilter;
import com.hcf.tax.taxauthgateway.filter.AuthGatewayFilterFactory;
import com.hcf.tax.taxauthgateway.filter.AuthGlobalFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @program: tax-auth-gateway
 * @description: 网关配置
 * @author: wanli
 * @create: 2019-05-30 14:48
 **/
@Configuration
public class GatewayRoutes {
    @Autowired
    AuthGatewayFilterFactory authGatewayFilterFactory;


    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                //r表示当然配置的路由   route
                .route(r ->
                        r.path("/java/**")
                                //stripPrefix  转发的时候跳过前缀
                                //比如，请求/name/bar/foo，去除掉前面两个前缀之后，最后转发到目标服务的路径为/foo
                                .filters(
                                        f -> f.stripPrefix(1)
                                )
                                .uri("http://localhost:8090/")
                                //.uri("http://httpbin.org")
                )
                //根据路径匹配
                .route("path_route", r -> r.path("/get")
                        .uri("http://httpbin.org"))

                //根据头部 heard的中的host来匹配
                .route("host_route", r -> r.host("*.myhost.org")
                        .uri("http://httpbin.org"))
                //更加方法类型匹配
 /*               .route("method_route", r -> r.method("Get")
                        .uri("http://httpbin.org"))*/
                //重写
                .route("rewrite_route", r -> r.host("*.rewrite.org")
                        .filters(f -> f.rewritePath("/foo/(?<segment>.*)", "/${segment}"))
                        .uri("http://httpbin.org"))
                //熔断
                .route("hystrix_route", r -> r.host("*.hystrix.org")
                        //这里一定要配置熔断器的名字
                        .filters(f -> f.hystrix(c -> c.setName("slowcmd")))
                        .uri("http://httpbin.org"))
                //熔断+回调方法
                .route("hystrix_fallback_route", r -> r.host("*.hystrixfallback.org")
                        .filters(f -> f.hystrix(c -> c.setName("slowcmd")
                                .setFallbackUri("forward:/hystrixfallback")))
                        .uri("http://httpbin.org"))
                //host+path双重限制   +  限流
                .route("limit_route", r -> r
                        .host("*.limited.org").and().path("/anything/**")
                        .filters(f -> f.requestRateLimiter(c ->
                                c.setRateLimiter(redisRateLimiter())
                                .setKeyResolver(ipKeyResolver())))
                        .uri("http://httpbin.org"))
                //根据路径匹配
                .route("auth_path_route", r -> r.path("/auth/test").filters(f ->
                        f.filter(new AuthGatewayFilter()).rewritePath("/auth/(?<segment>.*)", "/get")
                         )
                        .uri("http://httpbin.org"))

                .build();
    }


    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }

    @Bean
    RedisRateLimiter redisRateLimiter() {
        //令牌生产速度  1    令牌桶的容积
        return new RedisRateLimiter(1, 2);
    }

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.httpBasic().and()
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/anything/**").authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService reactiveUserDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
        return new MapReactiveUserDetailsService(user);
    }
}
