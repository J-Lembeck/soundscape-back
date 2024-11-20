package com.soundscape.soundscape.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import com.soundscape.soundscape.artist.ArtistService;
import com.soundscape.soundscape.artist.details.CustomUserDetailsService;
import com.soundscape.soundscape.artist.dto.ArtistRegistrationDTO;
import com.soundscape.soundscape.security.JwtTokenUtil;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAuthenticationToken_Success() throws Exception {
        AuthRequest authRequest = new AuthRequest("testuser", "password");
        UserDetails mockUserDetails = org.mockito.Mockito.mock(UserDetails.class);
        
        when(mockUserDetails.getUsername()).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);
        when(jwtTokenUtil.generateToken(mockUserDetails)).thenReturn("mocked_jwt_token");

        ResponseEntity<AuthResponse> response = authController.createAuthenticationToken(authRequest);

        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password")
        );
        verify(jwtTokenUtil, times(1)).generateToken(mockUserDetails);
        verify(userDetailsService, times(1)).loadUserByUsername("testuser");

        assert response.getBody() != null;
        assert response.getBody().getToken().equals("mocked_jwt_token");
        assert response.getBody().getUsername().equals("testuser");
    }

    @Test
    void testCreateAuthenticationToken_Failure() {
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");

        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "wrongpassword")
        );

        try {
            authController.createAuthenticationToken(authRequest);
        } catch (AuthenticationException e) {
        }

        verify(jwtTokenUtil, times(0)).generateToken(any(UserDetails.class));
    }

    @Test
    void testRegisterArtist_Success() {
        ArtistRegistrationDTO registrationDTO = new ArtistRegistrationDTO("newuser", "password", "email@test.com");

        when(artistService.registerArtist(registrationDTO))
                .thenReturn(ResponseEntity.ok("Artista registrado com sucesso."));

        ResponseEntity<Object> response = authController.createAuthenticationToken(registrationDTO);

        verify(artistService, times(1)).registerArtist(eq(registrationDTO));

        assert response.getBody() != null;
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Artista registrado com sucesso.", response.getBody());
    }

    @Test
    void testRegisterArtist_ValidationErrors() {
        ArtistRegistrationDTO registrationDTO = new ArtistRegistrationDTO("newuser", "password", "email@test.com");

        List<String> errors = List.of("Email já está em uso.", "Nome de usuário já está em uso.");
        when(artistService.registerArtist(registrationDTO))
                .thenReturn(ResponseEntity.status(HttpStatus.CONFLICT).body(errors));

        ResponseEntity<Object> response = authController.createAuthenticationToken(registrationDTO);

        verify(artistService, times(1)).registerArtist(eq(registrationDTO));

        assert response.getBody() != null;
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<String> responseErrors = (List<String>) response.getBody();
        assertEquals(2, responseErrors.size());
        assertTrue(responseErrors.contains("Email já está em uso."));
        assertTrue(responseErrors.contains("Nome de usuário já está em uso."));
    }

    @Test
    void testValidateToken_ValidToken() {
        String validToken = "Bearer valid_jwt_token";
        String username = "testuser";
        UserDetails mockUserDetails = org.mockito.Mockito.mock(UserDetails.class);

        when(jwtTokenUtil.getUsernameFromToken("valid_jwt_token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);
        when(jwtTokenUtil.validateToken("valid_jwt_token", mockUserDetails)).thenReturn(true);

        boolean isValid = authController.validateToken(validToken);

        verify(jwtTokenUtil, times(1)).getUsernameFromToken("valid_jwt_token");
        verify(userDetailsService, times(1)).loadUserByUsername(username);
        verify(jwtTokenUtil, times(1)).validateToken("valid_jwt_token", mockUserDetails);

        assert isValid;
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "Bearer invalid_jwt_token";

        when(jwtTokenUtil.getUsernameFromToken("invalid_jwt_token")).thenReturn(null);

        boolean isValid = authController.validateToken(invalidToken);

        verify(jwtTokenUtil, times(1)).getUsernameFromToken("invalid_jwt_token");
        verify(userDetailsService, times(0)).loadUserByUsername(any());
        verify(jwtTokenUtil, times(0)).validateToken(any(), any());

        assert !isValid;
    }
}
