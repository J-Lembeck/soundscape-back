package com.soundscape.soundscape.song;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.acrcloud.utils.ACRCloudRecognizer;
import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;
import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.audiofile.AudioFileRepository;
import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;
import com.soundscape.soundscape.song.image.SongImageRepository;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

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
    private S3Client s3Client;

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
        artist.setLikedSongs(new HashSet<>(Set.of(song)));

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
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));

        MultipartFile audioFileMock = mock(MultipartFile.class);
        when(audioFileMock.getOriginalFilename()).thenReturn("testAudio.mp3");
        when(audioFileMock.getBytes()).thenThrow(new IOException("Test exception"));

        SongUploadDTO songData = new SongUploadDTO();
        songData.setAudioFile(audioFileMock);

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

    @Test
    void saveSongWithAudio_CopyrightedSong() throws IOException {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));

        MockMultipartFile audioFileMock = new MockMultipartFile("audio", "testAudio.mp3", "audio/mpeg", new byte[]{});
        SongUploadDTO songData = new SongUploadDTO();
        songData.setAudioFile(audioFileMock);

        String acrResult = "{\"status\":{\"code\":0}, \"metadata\":{\"music\":[{\"title\":\"Copyrighted Song\",\"artists\":[{\"name\":\"Famous Artist\"}] }] }}";
        ACRCloudRecognizer recognizerMock = mock(ACRCloudRecognizer.class);
        when(recognizerMock.recognizeByFileBuffer(any(byte[].class), anyInt(), anyInt())).thenReturn(acrResult);

        SongService songServiceSpy = spy(songService);
        doReturn(recognizerMock).when(songServiceSpy).createRecognizer(any());

        ResponseEntity<String> response = songServiceSpy.saveSongWithAudio("testUser", songData);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("Upload failed: The song matches a copyrighted song"));
    }


    @Test
    void getSongImage_Success() {
        byte[] imageData = "image data".getBytes();
        when(songImageRepository.findImageBySongId(anyLong())).thenReturn(imageData);

        byte[] result = songService.getSongImage(1L);

        assertEquals(imageData, result);
        verify(songImageRepository, times(1)).findImageBySongId(anyLong());
    }

    @Test
    void getSongImage_NotFound() {
        when(songImageRepository.findImageBySongId(anyLong())).thenReturn(null);

        byte[] result = songService.getSongImage(1L);

        assertEquals(null, result);
        verify(songImageRepository, times(1)).findImageBySongId(anyLong());
    }

    @Test
    void listAllForLoggedUser_NoSongsFound() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));
        when(songRepository.findAllWithoutImageDataAndLikedStatus(anyLong())).thenReturn(new ArrayList<>());

        List<SongDTO> songs = songService.listAllForLoggedUser("testUser");

        assertTrue(songs.isEmpty());
        verify(songRepository, times(1)).findAllWithoutImageDataAndLikedStatus(anyLong());
    }

    @Test
    void listAll_SuccessWithOrder() {
        ArtistDTO artistDTO = new ArtistDTO(1L, "Artist 1");
        List<SongDTO> mockSongs = List.of(
                new SongDTO(2L, "Song B", artistDTO, new Date(), 200L),
                new SongDTO(1L, "Song A", artistDTO, new Date(), 180L)
        );

        when(songRepository.findAllWithoutImageDataOrderByCreationDate()).thenReturn(mockSongs);

        List<SongDTO> songs = songService.listAll();

        assertEquals(2, songs.size());
        assertEquals("Song B", songs.get(0).getTitle());
        verify(songRepository, times(1)).findAllWithoutImageDataOrderByCreationDate();
    }

    @Test
    void downloadAudioFile_ArtistNotFound() {
        when(artistRepository.findByName("testUser")).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = songService.downloadAudioFile("testUser", 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error: Artist not found", new String(response.getBody()));
    }

    @Test
    void downloadAudioFile_SongNotFound() {
        ArtistModel artist = new ArtistModel();
        artist.setName("testUser");
        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(artist));
        when(songRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = songService.downloadAudioFile("testUser", 1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error: Song not found", new String(response.getBody()));
    }

    @Test
    void downloadAudioFile_InternalServerError() {
        ArtistModel artist = new ArtistModel();
        artist.setName("testUser");
        SongModel song = new SongModel();
        AudioFileModel audioFile = new AudioFileModel();
        audioFile.setFilePath("audio/testFile.mp3");
        song.setAudioFile(audioFile);

        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(artist));
        when(songRepository.findById(1L)).thenReturn(Optional.of(song));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(RuntimeException.class);

        ResponseEntity<byte[]> response = songService.downloadAudioFile("testUser", 1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void testCreateRecognizer() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "https://test.acrcloud.com");
        config.put("access_key", "testAccessKey");
        config.put("access_secret", "testAccessSecret");
        config.put("timeout", 10);

        ACRCloudRecognizer recognizer = songService.createRecognizer(config);

        assertNotNull(recognizer, "ACRCloudRecognizer should not be null");
    }

    @Test
    void testCreateMp3File() throws Exception {
        File mp3File = new File("src/test/resources/test-file.mp3");
        assertTrue(mp3File.exists(), "The test MP3 file should exist");

        Mp3File result = songService.createMp3File(mp3File);

        assertNotNull(result, "Mp3File should not be null");
    }

    @Test
    void testCreateS3Client() {
        System.setProperty("AWS_ACCESS_KEY_ID", "testAccessKeyId");
        System.setProperty("AWS_ACCESS_KEY_SECRET", "testSecretAccessKey");

        SongService songService = new SongService();

        S3Client s3Client = songService.createS3Client();

        assertNotNull(s3Client, "S3Client should not be null");

        System.clearProperty("AWS_ACCESS_KEY_ID");
        System.clearProperty("AWS_ACCESS_KEY_SECRET");
    }
}
