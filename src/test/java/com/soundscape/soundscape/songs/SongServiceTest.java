package com.soundscape.soundscape.songs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
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
import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.audiofile.AudioFileRepository;
import com.soundscape.soundscape.song.SongModel;
import com.soundscape.soundscape.song.SongRepository;
import com.soundscape.soundscape.song.SongService;
import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;
import com.soundscape.soundscape.song.image.SongImageRepository;

class SongServiceTest {
	
    @InjectMocks
    private SongService songService;

    @Mock
    private SongRepository songRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private AudioFileRepository audioFileRepository;

    @Mock
    private SongImageRepository songImageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveSongWithAudio_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());
        SongUploadDTO songData = new SongUploadDTO();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            songService.saveSongWithAudio("testUser", songData);
        });

        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void likeSong_AddLike() {
        ArtistModel artist = new ArtistModel();
        SongModel song = new SongModel();
        song.setLikes(0L);
        artist.setLikedSongs(new HashSet<>());

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));

        ResponseEntity<String> response = songService.likeSong("testUser", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Song added to liked songs successfully.", response.getBody());
        assertEquals(1L, song.getLikes());
        verify(songRepository, times(1)).save(song);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void likeSong_RemoveLike() {
        ArtistModel artist = new ArtistModel();
        SongModel song = new SongModel();
        song.setLikes(1L);
        artist.setLikedSongs(Set.of(song));

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));

        ResponseEntity<String> response = songService.likeSong("testUser", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Song removed from liked songs successfully.", response.getBody());
        assertEquals(0L, song.getLikes());
        verify(songRepository, times(1)).save(song);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void listAllForLoggedUser_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            songService.listAllForLoggedUser("testUser");
        });

        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void searchSongsForLoggedUser_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            songService.searchSongsForLoggedUser("test", "testUser");
        });

        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void searchSongsForLoggedUser_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        ArtistDTO artistDTO = new ArtistDTO(1L, "Artist 1");

        List<SongDTO> mockSongs = List.of(
            new SongDTO(1L, "Song 1", artistDTO, new Date(), 200L),
            new SongDTO(2L, "Song 2", artistDTO, new Date(), 180L)
        );

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(songRepository.searchByTitleOrArtistNameAndLiked(anyString(), anyLong())).thenReturn(mockSongs);

        List<SongDTO> foundSongs = songService.searchSongsForLoggedUser("test", "testUser");

        assertEquals(2, foundSongs.size());
        assertEquals("Song 1", foundSongs.get(0).getTitle());
        verify(songRepository, times(1)).searchByTitleOrArtistNameAndLiked(anyString(), anyLong());
    }

    @Test
    void searchSongs_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        SongModel song1 = new SongModel();
        song1.setId(1L);
        song1.setTitle("Song 1");
        song1.setArtist(artist);
        song1.setLength(200L);
        song1.setLikes(10L);

        SongModel song2 = new SongModel();
        song2.setId(2L);
        song2.setTitle("Song 2");
        song2.setArtist(artist);
        song2.setLength(300L);
        song2.setLikes(20L);

        List<SongModel> mockSongs = List.of(song1, song2);

        when(songRepository.searchByTitleOrArtistName(anyString())).thenReturn(mockSongs);

        List<SongDTO> foundSongs = songService.searchSongs("test");

        assertEquals(2, foundSongs.size());
        assertEquals("Song 1", foundSongs.get(0).getTitle());
        assertEquals("Song 2", foundSongs.get(1).getTitle());
        verify(songRepository, times(1)).searchByTitleOrArtistName(anyString());
    }

    @Test
    void findLikedSongsFromArtist_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            songService.findLikedSongsFromArtist("testUser");
        });

        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void findLikedSongsFromArtist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);

        SongModel likedSong = new SongModel();
        likedSong.setTitle("Liked Song");
        likedSong.setArtist(artist);
        likedSong.setLength(200L);
        likedSong.setLikes(10L);

        artist.setLikedSongs(Set.of(likedSong));

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));

        List<SongDTO> likedSongs = songService.findLikedSongsFromArtist("testUser");

        assertEquals(1, likedSongs.size());
        assertEquals("Liked Song", likedSongs.get(0).getTitle());
        verify(artistRepository, times(1)).findByName(anyString());
    }

    @Test
    void saveSongWithAudio_ExceptionHandling() throws IOException {
        when(artistRepository.findByName(anyString())).thenThrow(new RuntimeException("Test exception"));
        SongUploadDTO songData = new SongUploadDTO();

        ResponseEntity<String> response = songService.saveSongWithAudio("testUser", songData);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to upload song and image: Test exception", response.getBody());
    }

    @Test
    void listAll_Success() {
    	ArtistDTO artistDTO = new ArtistDTO(1L, "Artist 1");
        List<SongDTO> mockSongs = List.of(
                new SongDTO(1L, "Song 1", artistDTO, new Date(), 200L),
                new SongDTO(2L, "Song 2", artistDTO, new Date(), 180L)
            );
        when(songRepository.findAllWithoutImageDataOrderByCreationDate()).thenReturn(mockSongs);

        List<SongDTO> songs = songService.listAll();

        assertEquals(2, songs.size());
        verify(songRepository, times(1)).findAllWithoutImageDataOrderByCreationDate();
    }
}
