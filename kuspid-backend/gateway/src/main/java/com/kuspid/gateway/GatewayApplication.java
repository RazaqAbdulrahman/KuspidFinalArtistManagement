package com.kuspid.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import com.kuspid.gateway.filter.JwtAuthenticationFilter;

@SpringBootApplication
public class GatewayApplication {

        public static void main(String[] args) {
                SpringApplication.run(GatewayApplication.class, args);
        }

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthenticationFilter authFilter) {
                return builder.routes()
                                .route("auth-service", r -> r.path("/api/auth/**")
                                                .uri("http://localhost:8081"))
                                .route("beat-service", r -> r.path("/api/beats/**")
                                                .filters(f -> f.filter(
                                                                authFilter.apply(new JwtAuthenticationFilter.Config())))
                                                .uri("http://localhost:8082"))
                                .route("artist-service", r -> r.path("/api/artists/**")
                                                .filters(f -> f.filter(
                                                                authFilter.apply(new JwtAuthenticationFilter.Config())))
                                                .uri("http://localhost:8083"))
                                .route("analytics-service", r -> r.path("/api/analytics/**")
                                                .filters(f -> f.filter(
                                                                authFilter.apply(new JwtAuthenticationFilter.Config())))
                                                .uri("http://localhost:8084"))
                                .build();
        }
}
