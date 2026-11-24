package br.edu.atitus.bompradev.gateway.configs;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {
    @Bean
    RouteLocator rotas(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", rota -> rota.path("/auth/**", "/perfis/**").uri("lb://auth-service"))
                .route("catalog-service", rota -> rota.path("/catalogo/**").uri("lb://catalog-service"))
                .route("scheduling-service", rota -> rota.path("/agenda/**").uri("lb://scheduling-service"))
                .build();
    }

    @Bean
    SecurityWebFilterChain seguranca(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll()).build();
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.addAllowedOriginPattern("*");
        cors.addAllowedMethod("GET");
        cors.addAllowedMethod("POST");
        cors.addAllowedMethod("PUT");
        cors.addAllowedMethod("DELETE");
        cors.addAllowedHeader("Authorization");
        cors.addAllowedHeader("Content-Type");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsWebFilter(source);
    }
}
