package com.soundscape.soundscape.song;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.soundscape.soundscape.song.dto.SongUploadDTO;

public class SongControllerTest {

    @Mock
    private SongService songService;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private SongController songController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listAllOrForLoggedUser_whenLoggedIn_shouldReturnUserSongs() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        List<SongDTO> userSongs = Collections.singletonList(new SongDTO());

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(songService.listAllForLoggedUser(username)).thenReturn(userSongs);

        List<SongDTO> result = songController.listAllOrForLoggedUser(authHeader);

        assertEquals(userSongs, result);
        verify(songService).listAllForLoggedUser(username);
    }

    @Test
    void listAllOrForLoggedUser_whenNotLoggedIn_shouldReturnAllSongs() {
        List<SongDTO> allSongs = Collections.singletonList(new SongDTO());
        when(songService.listAll()).thenReturn(allSongs);

        List<SongDTO> result = songController.listAllOrForLoggedUser(null);

        assertEquals(allSongs, result);
        verify(songService).listAll();
    }

    @Test
    void getSongImage_shouldReturnImageBytes() {
        Long songId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        when(songService.getSongImage(songId)).thenReturn(imageBytes);

        byte[] result = songController.getSongImage(songId);

        assertEquals(imageBytes, result);
        verify(songService).getSongImage(songId);
    }

    @Test
    void uploadSong_shouldCallSaveSongWithAudio() throws IOException {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        SongUploadDTO songData = new SongUploadDTO();
        ResponseEntity<String> response = ResponseEntity.ok("Song uploaded");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(songService.saveSongWithAudio(username, songData)).thenReturn(response);

        ResponseEntity<String> result = songController.uploadSong(authHeader, songData);

        assertEquals(response, result);
        verify(songService).saveSongWithAudio(username, songData);
    }

    @Test
    void searchSongs_whenLoggedIn_shouldReturnUserSearchResults() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        String searchTerm = "test";
        List<SongDTO> searchResults = Collections.singletonList(new SongDTO());

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(songService.searchSongsForLoggedUser(searchTerm, username)).thenReturn(searchResults);

        List<SongDTO> result = songController.searchSongs(authHeader, searchTerm);

        assertEquals(searchResults, result);
        verify(songService).searchSongsForLoggedUser(searchTerm, username);
    }

    @Test
    void searchSongs_whenNotLoggedIn_shouldReturnAllSearchResults() {
        String searchTerm = "test";
        List<SongDTO> searchResults = Collections.singletonList(new SongDTO());

        when(songService.searchSongs(searchTerm)).thenReturn(searchResults);

        List<SongDTO> result = songController.searchSongs(null, searchTerm);

        assertEquals(searchResults, result);
        verify(songService).searchSongs(searchTerm);
    }

    @Test
    void findLikedSongsFromLoggedUser_shouldReturnLikedSongs() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        List<SongDTO> likedSongs = Collections.singletonList(new SongDTO());

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(songService.findLikedSongsFromArtist(username)).thenReturn(likedSongs);

        List<SongDTO> result = songController.findLikedSongsFromLoggedUser(authHeader);

        assertEquals(likedSongs, result);
        verify(songService).findLikedSongsFromArtist(username);
    }

    @Test
    void likeSong_shouldLikeSong() {
        String authHeader = "Bearer test-token";
        String username = "testUser";
        Long songId = 1L;
        ResponseEntity<String> response = ResponseEntity.ok("Song liked");

        when(jwtTokenUtil.getUsernameFromToken("test-token")).thenReturn(username);
        when(songService.likeSong(username, songId)).thenReturn(response);

        ResponseEntity<String> result = songController.findLikedSongsFromLoggedUser(authHeader, songId);

        assertEquals(response, result);
        verify(songService).likeSong(username, songId);
    }
}
