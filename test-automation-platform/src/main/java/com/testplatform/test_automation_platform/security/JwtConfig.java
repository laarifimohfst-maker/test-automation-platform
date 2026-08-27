package com.testplatform.test_automation_platform.security;

import com.testplatform.test_automation_platform.entity.Utilisateur;
import com.testplatform.test_automation_platform.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class JwtConfig {

    @Bean
    public KeyPair jwtKeyPair(
            @Value("${security.jwt.key-directory}") String keyDirectory)
            throws GeneralSecurityException, IOException {

        Path directory = Paths.get(keyDirectory).toAbsolutePath().normalize();
        Path privateKeyPath = directory.resolve("private.key");
        Path publicKeyPath = directory.resolve("public.key");

        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            return chargerPaireDeCles(privateKeyPath, publicKeyPath);
        }

        if (Files.exists(privateKeyPath) || Files.exists(publicKeyPath)) {
            throw new IllegalStateException(
                    "La configuration JWT est incomplète : une seule clé RSA existe."
            );
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Files.createDirectories(directory);
        Files.writeString(
                privateKeyPath,
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );
        Files.writeString(
                publicKeyPath,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );

        return keyPair;
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) jwtKeyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) jwtKeyPair.getPrivate();

        return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            KeyPair jwtKeyPair,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.audience}") String audience,
            UtilisateurRepository utilisateurRepository) {

        RSAPublicKey publicKey = (RSAPublicKey) jwtKeyPair.getPublic();
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audiences -> audiences != null && audiences.contains(audience)
                );
        OAuth2TokenValidator<Jwt> utilisateurActifValidator =
                new JwtClaimValidator<String>(
                        "sub",
                        email -> email != null
                                && utilisateurRepository
                                .findByEmailIgnoreCase(email)
                                .map(Utilisateur::isActif)
                                .orElse(false)
                );

        jwtDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator,
                        utilisateurActifValidator
                )
        );
        return jwtDecoder;
    }

    private KeyPair chargerPaireDeCles(
            Path privateKeyPath,
            Path publicKeyPath) throws GeneralSecurityException, IOException {

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] privateKeyBytes = Base64.getDecoder().decode(
                Files.readString(privateKeyPath).trim()
        );
        byte[] publicKeyBytes = Base64.getDecoder().decode(
                Files.readString(publicKeyPath).trim()
        );

        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(privateKeyBytes)
        );
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(publicKeyBytes)
        );

        return new KeyPair(publicKey, privateKey);
    }
}
