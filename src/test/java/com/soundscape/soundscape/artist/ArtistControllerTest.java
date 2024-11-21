package com.soundscape.soundscape.artist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.security.JwtTokenUtil;
import com.soundscape.soundscape.song.dto.SongDTO;

public class ArtistControllerTest {

    @Mock
    private ArtistService artistService;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private ArtistController artistController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_shouldReturnArtist() {
        ArtistDTO artist = new ArtistDTO(1L, "Test Artist");
        when(artistService.findById(1L)).thenReturn(artist);

        ArtistDTO result = artistController.findById(1L);

        assertEquals(artist, result);
        verify(artistService).findById(1L);
    }

    @Test
    void findSongsFromArtist_shouldReturnSongs() {
        List<SongDTO> songs = Collections.singletonList(new SongDTO(1L, "Test Song", null, null, null));
        when(artistService.findSongsFromArtist(1L)).thenReturn(songs);

        List<SongDTO> result = artistController.findSongsFromArtist(1L);

        assertEquals(songs, result);
        verify(artistService).findSongsFromArtist(1L);
    }

    @Test
    void findArtistFromLoggedUser_shouldReturnArtist() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        ArtistDTO artist = new ArtistDTO(1L, "Logged User Artist");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(artistService.findArtistFromLoggedUser(username)).thenReturn(artist);

        ArtistDTO result = artistController.findArtistFromLoggedUser(authHeader);

        assertEquals(artist, result);
        verify(artistService).findArtistFromLoggedUser(username);
    }

    @Test
    void followArtist_shouldFollowArtist() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        Long artistId = 1L;
        ResponseEntity<String> response = ResponseEntity.ok("Artista seguido com sucesso.");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(artistService.followArtist(username, artistId)).thenReturn(response);

        ResponseEntity<String> result = artistController.followArtist(authHeader, artistId);

        assertEquals(response, result);
        verify(artistService).followArtist(username, artistId);
    }

    @Test
    void findFollowedByUser_shouldReturnFollowedArtists() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        List<ArtistDTO> followedArtists = Collections.singletonList(new ArtistDTO(2L, "Followed Artist"));

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(artistService.findArtistsFollowedByUser(username)).thenReturn(followedArtists);

        List<ArtistDTO> result = artistController.findFollowedByUser(authHeader);

        assertEquals(followedArtists, result);
        verify(artistService).findArtistsFollowedByUser(username);
    }

    @Test
    void unfollowArtist_shouldUnfollowArtist() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        Long artistId = 1L;
        ResponseEntity<String> response = ResponseEntity.ok("Você deixou de seguir o artista com sucesso.");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(artistService.unfollowArtist(username, artistId)).thenReturn(response);

        ResponseEntity<String> result = artistController.unfollowArtist(authHeader, artistId);

        assertEquals(response, result);
        verify(artistService).unfollowArtist(username, artistId);
    }

    @Test
    void findFollowersOfUser_shouldReturnFollowers() {
        Long artistId = 1L;
        List<ArtistDTO> followers = Collections.singletonList(new ArtistDTO(3L, "Follower Artist"));

        when(artistService.findFollowersOfUser(artistId)).thenReturn(followers);

        List<ArtistDTO> result = artistController.findFollowersOfUser(artistId);

        assertEquals(followers, result);
        verify(artistService).findFollowersOfUser(artistId);
    }

    @Test
    void findArtistFromLoggedUser_withInvalidToken_shouldThrowException() {
        String authHeader = "Bearer invalid-token";

        when(jwtTokenUtil.getUsernameFromToken("invalid-token")).thenThrow(new IllegalArgumentException("Invalid token"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            artistController.findArtistFromLoggedUser(authHeader);
        });

        assertEquals("Invalid token", exception.getMessage());
        verify(jwtTokenUtil).getUsernameFromToken("invalid-token");
        verifyNoInteractions(artistService);
    }
}
