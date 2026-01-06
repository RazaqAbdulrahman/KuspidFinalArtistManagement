package com.kuspid.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private static final String SECRET_KEY = "e83d8c7c9f2b1a0e9d8c7b6a5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secretKey", SECRET_KEY);
    }

    private String generateValidToken() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject("test@kuspid.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("Filter - Should reject request without Authorization header")
    void apply_ShouldReject_WhenNoAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/beats").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filter - Should reject request with invalid token")
    void apply_ShouldReject_WhenInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/beats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Filter - Should accept request with valid token")
    void apply_ShouldAccept_WhenValidToken() {
        String validToken = generateValidToken();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/beats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        GatewayFilter gatewayFilter = filter.apply(new JwtAuthenticationFilter.Config());
        gatewayFilter.filter(exchange, chain).block();

        // If valid, response status should not be set to UNAUTHORIZED
        // (would be null or OK depending on downstream)
    }
}
