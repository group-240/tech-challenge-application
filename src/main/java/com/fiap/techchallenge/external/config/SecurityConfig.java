package com.fiap.techchallenge.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/health", "/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                
                // Endpoints que não requerem autenticação (conforme regra de negócio)
                .requestMatchers("/api/customers").permitAll() // Cadastro de cliente
                .requestMatchers("/api/categories/**").permitAll() // Consulta de categorias
                .requestMatchers("/api/products/**").permitAll() // Consulta de produtos
                .requestMatchers("/api/orders").permitAll() // Criação de pedido (pode ser anônimo)
                .requestMatchers("/api/payments/webhook").permitAll() // Webhook do Mercado Pago
                
                // Todos os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public org.springframework.core.convert.converter.Converter<org.springframework.security.oauth2.jwt.Jwt, org.springframework.security.authentication.AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
    }
}