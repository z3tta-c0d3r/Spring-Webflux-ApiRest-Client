package com.example.demo;

import com.example.demo.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> pathHandler(ProductHandler handler) {
        return route(GET("/api/client"), handler::list)
                .andRoute(GET("/api/client/{id}"), handler::see)
                .andRoute(POST("/api/client"), handler::create)
                .andRoute(PUT("/api/client/{id}"), handler::edit)
                .andRoute(DELETE("/api/client/{id}"), handler::delete)
                .andRoute(POST("/api/client/upload/{id}"), handler::upload);

    }
}