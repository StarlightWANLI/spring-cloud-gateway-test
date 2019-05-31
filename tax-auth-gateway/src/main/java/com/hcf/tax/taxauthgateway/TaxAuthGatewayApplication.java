package com.hcf.tax.taxauthgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaxAuthGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxAuthGatewayApplication.class, args);
    }


    /**
     * 创建的route可以让请求“/get”请求都转发到“http://httpbin.org/get”。
     * 在route配置上，我们添加了一个filter，该filter会将请求添加一个header,key为hello，value为world。
     * @param builder
     * @return
     */
  /*  @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        //配置一个route，在route中添加一个filters
        return builder.routes()
                .route("path_route",p -> p
                        .path("/get")
                     //   .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri("http://httpbin.org"))
                .build();
    }*/

}
