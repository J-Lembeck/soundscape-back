package com.soundscape.soundscape.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private String secret = "testsecret";

    @BeforeEach
    public void setUp() throws Exception {
        jwtTokenUtil = new JwtTokenUtil();

        Field secretField = JwtTokenUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenUtil, secret);
    }

    @Test
    public void testGenerateToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("usuarioTeste");

        String token = jwtTokenUtil.generateToken(userDetails);

        assertNotNull(token, "O token não deve ser nulo");
    }

    @Test
    public void testGetUsernameFromToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("usuarioTeste");

        String token = jwtTokenUtil.generateToken(userDetails);

        String username = jwtTokenUtil.getUsernameFromToken(token);

        assertEquals("usuarioTeste", username, "O nome de usuário deve ser 'usuarioTeste'");
    }

    @Test
    public void testValidateToken_Valid() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("usuarioTeste");

        String token = jwtTokenUtil.generateToken(userDetails);

        boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

        assertTrue(isValid, "O token deve ser válido");
    }

    @Test
    public void testValidateToken_InvalidUsername() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("usuarioTeste");

        String token = jwtTokenUtil.generateToken(userDetails);

        UserDetails otherUserDetails = mock(UserDetails.class);
        when(otherUserDetails.getUsername()).thenReturn("outroUsuario");

        boolean isValid = jwtTokenUtil.validateToken(token, otherUserDetails);

        assertFalse(isValid, "O token não deve ser válido para um nome de usuário diferente");
    }

    @Test
    public void testValidateToken_Expired() throws Exception {
        Field secretField = JwtTokenUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenUtil, secret);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("usuarioTeste");

        String expiredToken = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        boolean isValid = jwtTokenUtil.validateToken(expiredToken, userDetails);

        assertFalse(isValid, "O token deve estar expirado e, portanto, inválido");
    }

    @Test
    public void testValidateToken_InvalidSignature() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("usuarioTeste");

        String invalidToken = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(SignatureAlgorithm.HS512, "outraChaveSecreta")
                .compact();

        boolean isValid = jwtTokenUtil.validateToken(invalidToken, userDetails);

        assertFalse(isValid, "O token não deve ser válido devido à assinatura incorreta");
    }


}