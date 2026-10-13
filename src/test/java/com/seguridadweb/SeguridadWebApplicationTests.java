package com.seguridadweb;

import com.seguridadweb.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SeguridadWebApplicationTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        assertNotNull(jwtUtil);
        assertNotNull(passwordEncoder);
    }

    @Test
    void testPasswordHashing() {
        String rawPassword = "password123";
        String encoded = passwordEncoder.encode(rawPassword);
        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    void testJwtGenerationAndValidation() {
        String email = "test@seguridad.com";
        String rol = "ROLE_USER";

        String token = jwtUtil.generateToken(email, rol);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertFalse(jwtUtil.isTokenExpired(token));
        org.junit.jupiter.api.Assertions.assertEquals(email, jwtUtil.extractUsername(token));
    }
}
