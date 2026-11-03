package com.smarthr.auth.controller;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksController {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {
        try {
            File file = ResourceUtils.getFile(publicKeyPath);
            String key = new String(Files.readAllBytes(file.toPath()));
            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                     .replace("-----END PUBLIC KEY-----", "")
                     .replaceAll("\\s+", "");

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            String n = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
            String e = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

            Map<String, Object> jwk = Map.of(
                    "kty", "RSA",
                    "alg", "RS256",
                    "use", "sig",
                    "n", n,
                    "e", e
            );

            return Map.of("keys", List.of(jwk));
        } catch (Exception ex) {
            throw new RuntimeException("Error generating JWKS", ex);
        }
    }
}