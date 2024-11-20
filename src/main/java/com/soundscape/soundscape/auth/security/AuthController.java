package com.soundscape.soundscape.auth.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soundscape.soundscape.artist.ArtistService;
import com.soundscape.soundscape.artist.details.CustomUserDetailsService;
import com.soundscape.soundscape.artist.dto.ArtistRegistrationDTO;
import com.soundscape.soundscape.security.JwtTokenUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ArtistService artistService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService, ArtistService artistService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.artistService = artistService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws AuthenticationException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token, userDetails.getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createAuthenticationToken(@RequestBody ArtistRegistrationDTO registrationDTO) {
    	return artistService.registerArtist(registrationDTO);
    }

    @GetMapping("/validate-token")
    public boolean validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }

        String jwtToken = token.substring(7);
        String username = jwtTokenUtil.getUsernameFromToken(jwtToken);

        if (username == null) {
            return false;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtTokenUtil.validateToken(jwtToken, userDetails);
    }
}
