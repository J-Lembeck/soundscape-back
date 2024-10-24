package com.soundscape.soundscape.playlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;
import com.soundscape.soundscape.song.SongModel;
import com.soundscape.soundscape.song.SongRepository;
import com.soundscape.soundscape.song.dto.SongDTO;

class PlaylistServiceTest {

    @InjectMocks
    private PlaylistService playlistService;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SongRepository songRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllByArtist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        PlaylistModel playlist = new PlaylistModel();
        playlist.setName("Test Playlist");
        playlist.setArtist(artist);

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(playlistRepository.findAllByArtistId(anyLong())).thenReturn(List.of(playlist));

        List<PlaylistDTO> playlists = playlistService.findAllByArtist("Test Artist");

        assertEquals(1, playlists.size());
        assertEquals("Test Playlist", playlists.get(0).getName());
        verify(artistRepository, times(1)).findByName(anyString());
        verify(playlistRepository, times(1)).findAllByArtistId(anyLong());
    }

    @Test
    void findAllByArtist_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistService.findAllByArtist("Nonexistent Artist");
        });
        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void createNewPlaylist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(playlistRepository.save(any(PlaylistModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaylistDTO createdPlaylist = playlistService.createNewPlaylist("Test Artist", "New Playlist");

        assertEquals("New Playlist", createdPlaylist.getName());
        verify(artistRepository, times(1)).findByName(anyString());
        verify(playlistRepository, times(1)).save(any(PlaylistModel.class));
    }

    @Test
    void createNewPlaylist_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistService.createNewPlaylist("Nonexistent Artist", "New Playlist");
        });
        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void findSongsFromPlaylist_Success() {
        PlaylistModel playlist = new PlaylistModel();
        playlist.setId(1L);

        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        SongModel song = new SongModel();
        song.setId(1L);
        song.setTitle("Test Song");
        song.setArtist(artist);

        playlist.setSongs(Set.of(song));

        when(playlistRepository.findById(anyLong())).thenReturn(Optional.of(playlist));

        List<SongDTO> songs = playlistService.findSongsFromPlaylist(1L);

        assertEquals(1, songs.size());
        assertEquals("Test Song", songs.get(0).getTitle());
        verify(playlistRepository, times(1)).findById(anyLong());
    }

    @Test
    void findSongsFromPlaylist_PlaylistNotFound() {
        when(playlistRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playlistService.findSongsFromPlaylist(1L);
        });
        assertEquals("Playlist not found", exception.getMessage());
    }

    @Test
    void addSongToPlaylist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        SongModel song = new SongModel();
        song.setId(1L);
        song.setTitle("New Song");

        PlaylistModel playlist = new PlaylistModel();
        playlist.setId(1L);
        playlist.setArtist(artist);
        playlist.setSongs(new HashSet<>(Set.of()));

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(playlistRepository.findById(anyLong())).thenReturn(Optional.of(playlist));
        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));

        ResponseEntity<String> response = playlistService.addSongToPlaylist(1L, 1L, "Test Artist");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Song added to the playlist successfully.", response.getBody());
        verify(playlistRepository, times(1)).save(any(PlaylistModel.class));
    }

    @Test
    void addSongToPlaylist_AlreadyExists() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        SongModel song = new SongModel();
        song.setId(1L);
        song.setTitle("Existing Song");

        PlaylistModel playlist = new PlaylistModel();
        playlist.setId(1L);
        playlist.setArtist(artist);
        playlist.setSongs(new HashSet<>(Set.of(song)));

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(playlistRepository.findById(anyLong())).thenReturn(Optional.of(playlist));
        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));

        ResponseEntity<String> response = playlistService.addSongToPlaylist(1L, 1L, "Test Artist");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("The song is already in the playlist.", response.getBody());
        verify(playlistRepository, never()).save(any(PlaylistModel.class));
    }

    @Test
    void removeSongFromPlaylist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        SongModel song = new SongModel();
        song.setId(1L);
        song.setTitle("Song to Remove");

        PlaylistModel playlist = new PlaylistModel();
        playlist.setId(1L);
        playlist.setArtist(artist);
        playlist.setSongs(new HashSet<>(Set.of(song)));

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(playlistRepository.findById(anyLong())).thenReturn(Optional.of(playlist));
        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));

        ResponseEntity<String> response = playlistService.removeSongFromPlaylist(1L, 1L, "Test Artist");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Song removed from the playlist successfully.", response.getBody());
        verify(playlistRepository, times(1)).save(any(PlaylistModel.class));
    }

    @Test
    void deletePlaylist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        PlaylistModel playlist = new PlaylistModel();
        playlist.setId(1L);
        playlist.setArtist(artist);

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(playlistRepository.findById(anyLong())).thenReturn(Optional.of(playlist));

        ResponseEntity<String> response = playlistService.deletePlaylist(1L, "Test Artist");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Playlist deleted.", response.getBody());
        verify(playlistRepository, times(1)).deleteById(anyLong());
    }
}
