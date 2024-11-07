package com.soundscape.soundscape.playlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.soundscape.soundscape.security.JwtTokenUtil;
import com.soundscape.soundscape.song.dto.SongDTO;

public class PlaylistControllerTest {

    @Mock
    private PlaylistService playlistService;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private PlaylistController playlistController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllByLoggedUser_shouldReturnPlaylists() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        List<PlaylistDTO> playlists = Collections.singletonList(new PlaylistDTO());

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(playlistService.findAllByArtist(username)).thenReturn(playlists);

        List<PlaylistDTO> result = playlistController.findAllByLoggedUser(authHeader);

        assertEquals(playlists, result);
        verify(playlistService).findAllByArtist(username);
    }

    @Test
    void createNewPlaylist_shouldReturnCreatedPlaylist() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        String playlistName = "My Playlist";
        PlaylistDTO createdPlaylist = new PlaylistDTO();

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(playlistService.createNewPlaylist(username, playlistName)).thenReturn(createdPlaylist);

        PlaylistDTO result = playlistController.createNewPlaylist(authHeader, playlistName);

        assertEquals(createdPlaylist, result);
        verify(playlistService).createNewPlaylist(username, playlistName);
    }

    @Test
    void findSongsFromPlaylist_shouldReturnSongs() {
        Long playlistId = 1L;
        List<SongDTO> songs = Collections.singletonList(new SongDTO());

        when(playlistService.findSongsFromPlaylist(playlistId)).thenReturn(songs);

        List<SongDTO> result = playlistController.findSongsFromPlaylist(playlistId);

        assertEquals(songs, result);
        verify(playlistService).findSongsFromPlaylist(playlistId);
    }

    @Test
    void addSongToPlaylist_shouldReturnSuccessResponse() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        Long playlistId = 1L;
        Long songId = 1L;
        ResponseEntity<String> response = ResponseEntity.ok("Song added to playlist");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(playlistService.addSongToPlaylist(playlistId, songId, username)).thenReturn(response);

        ResponseEntity<String> result = playlistController.addSongToPlaylist(authHeader, playlistId, songId);

        assertEquals(response, result);
        verify(playlistService).addSongToPlaylist(playlistId, songId, username);
    }

    @Test
    void removeSongFromPlaylist_shouldReturnSuccessResponse() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        Long playlistId = 1L;
        Long songId = 1L;
        ResponseEntity<String> response = ResponseEntity.ok("Song removed from playlist");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(playlistService.removeSongFromPlaylist(playlistId, songId, username)).thenReturn(response);

        ResponseEntity<String> result = playlistController.removeSongFromPlaylist(authHeader, playlistId, songId);

        assertEquals(response, result);
        verify(playlistService).removeSongFromPlaylist(playlistId, songId, username);
    }

    @Test
    void deletePlaylist_shouldReturnSuccessResponse() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        Long playlistId = 1L;
        ResponseEntity<String> response = ResponseEntity.ok("Playlist deleted");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(playlistService.deletePlaylist(playlistId, username)).thenReturn(response);

        ResponseEntity<String> result = playlistController.deletePlaylist(authHeader, playlistId);

        assertEquals(response, result);
        verify(playlistService).deletePlaylist(playlistId, username);
    }
}
