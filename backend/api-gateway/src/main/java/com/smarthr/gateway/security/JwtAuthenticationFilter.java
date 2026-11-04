package com.smarthr.gateway.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final WebClient webClient;
    private PublicKey cachedPublicKey;

    @Value("${jwt.jwks-uri}")
    private String jwksUri;

    public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            return getPublicKey()
                    .flatMap(publicKey -> {
                        try {
                            Claims claims = Jwts.parser()
                                    .verifyWith(publicKey)
                                    .build()
                                    .parseSignedClaims(token)
                                    .getPayload();

                            String userId = claims.getSubject();
                            String role = claims.get("role", String.class);
                            String companyId = claims.get("companyId", String.class);

                            ServerHttpRequest mutatedRequest = request.mutate()
                                    .header("X-User-Id", userId != null ? userId : "")
                                    .header("X-User-Role", role != null ? role : "")
                                    .header("X-Company-Id", companyId != null ? companyId : "")
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } catch (Exception e) {
                            return onError(exchange, HttpStatus.UNAUTHORIZED);
                        }
                    });
        };
    }

    private Mono<PublicKey> getPublicKey() {
        if (cachedPublicKey != null) {
            return Mono.just(cachedPublicKey);
        }

        return webClient.get()
                .uri(jwksUri)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        String pubKeyPEM = (String) response.get("publicKey");
                        byte[] encoded = Base64.getDecoder().decode(pubKeyPEM);
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        cachedPublicKey = keyFactory.generatePublic(keySpec);
                        return cachedPublicKey;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Mono<Void> onError(org.springframework.web.server.ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}