package com.soundscape.soundscape.songs;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;
import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.audiofile.AudioFileRepository;
import com.soundscape.soundscape.song.SongModel;
import com.soundscape.soundscape.song.SongRepository;
import com.soundscape.soundscape.song.SongService;
import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;
import com.soundscape.soundscape.song.image.SongImageModel;
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

    @Mock
    private Mp3File mp3File;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveSongWithAudio_Success() throws Exception {
        String userName = "Test Artist";
        MultipartFile audioFile = mock(MultipartFile.class);
        MultipartFile imageFile = mock(MultipartFile.class);
        SongUploadDTO songData = new SongUploadDTO("Test Title", audioFile, imageFile);

        ArtistModel artist = new ArtistModel();
        when(artistRepository.findByName(userName)).thenReturn(Optional.of(artist));

        when(audioFile.getOriginalFilename()).thenReturn("test.mp3");
        when(audioFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(audioFile.getSize()).thenReturn(1234L);

        when(mp3File.getLengthInSeconds()).thenReturn(120L);

        when(imageFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(IOUtils.toByteArray(imageFile.getInputStream())).thenReturn(new byte[0]);

        ResponseEntity<String> response = songService.saveSongWithAudio(userName, songData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Song and image uploaded successfully", response.getBody());
        verify(audioFileRepository).save(any(AudioFileModel.class));
        verify(songRepository).save(any(SongModel.class));
    }

    @Test
    void testSaveSongWithAudio_ArtistNotFound() throws IOException {
        String userName = "NonExistentArtist";
        SongUploadDTO songData = new SongUploadDTO("Test Title", null, null);

        when(artistRepository.findByName(userName)).thenReturn(Optional.empty());

        ResponseEntity<String> response = songService.saveSongWithAudio(userName, songData);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Artist not found"));
    }

    @Test
    void testGetSongImage() {
        Long songId = 1L;
        SongModel song = new SongModel();
        SongImageModel imageModel = new SongImageModel(new byte[]{1, 2, 3}, new Date());
        song.setSongImage(imageModel);

        when(songRepository.findById(songId)).thenReturn(Optional.of(song));

        byte[] imageData = songService.getSongImage(songId);

        assertArrayEquals(new byte[]{1, 2, 3}, imageData);
    }

    @Test
    void testListAll() {
        ArtistDTO artist1 = new ArtistDTO(1L, "Artist 1");
        ArtistDTO artist2 = new ArtistDTO(2L, "Artist 2");
        
        List<SongDTO> songs = Arrays.asList(
            new SongDTO(1L, "Song 1", 1L, artist1, new Date(), 300L, Boolean.TRUE),
            new SongDTO(2L, "Song 2", 1L, artist2, new Date(), 320L, Boolean.FALSE)
        );
        
        when(songRepository.findAllWithoutImageDataOrderByCreationDate()).thenReturn(songs);

        List<SongDTO> result = songService.listAll();

        assertEquals(2, result.size());
        assertEquals("Song 1", result.get(0).getTitle());
    }

    @Test
    void testSearchSongs() {
        String searchTerm = "test";
        SongModel song1 = new SongModel();
        SongModel song2 = new SongModel();
        when(songRepository.searchByTitleOrArtistName(searchTerm)).thenReturn(Arrays.asList(song1, song2));

        List<SongDTO> result = songService.searchSongs(searchTerm);

        assertEquals(2, result.size());
        verify(songRepository, times(1)).searchByTitleOrArtistName(searchTerm);
    }
}
