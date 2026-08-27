package com.testplatform.test_automation_platform;

import com.testplatform.test_automation_platform.security.JwtConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConfigTests {

    @TempDir
    Path temporaryDirectory;

    @Test
    void paireRsaEstRechargeeApresRedemarrage() throws Exception {
        JwtConfig configuration = new JwtConfig();

        KeyPair premiereLecture = configuration.jwtKeyPair(
                temporaryDirectory.toString()
        );
        KeyPair secondeLecture = configuration.jwtKeyPair(
                temporaryDirectory.toString()
        );

        assertThat(secondeLecture.getPrivate().getEncoded())
                .containsExactly(premiereLecture.getPrivate().getEncoded());
        assertThat(secondeLecture.getPublic().getEncoded())
                .containsExactly(premiereLecture.getPublic().getEncoded());
    }
}
