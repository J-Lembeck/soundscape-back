package com.soundscape.soundscape.artist.details;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ArtistRepository artistRepository;

    public CustomUserDetailsService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ArtistModel artist = artistRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(artist.getName())
                .password(artist.getPassword())
                .roles("USER")
                .build();
    }
}