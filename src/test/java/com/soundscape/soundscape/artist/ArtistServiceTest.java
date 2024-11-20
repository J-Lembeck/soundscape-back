package com.soundscape.soundscape.artist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.artist.dto.ArtistRegistrationDTO;
import com.soundscape.soundscape.song.SongModel;
import com.soundscape.soundscape.song.dto.SongDTO;

class ArtistServiceTest {

    @InjectMocks
    private ArtistService artistService;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerArtist_Success() {
        ArtistRegistrationDTO registrationDTO = new ArtistRegistrationDTO("testUser", "test@example.com", "password");

        when(artistRepository.existsByEmail(anyString())).thenReturn(false);
        when(artistRepository.existsByName(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<Object> response = artistService.registerArtist(registrationDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Artista registrado com sucesso.", response.getBody());
        verify(artistRepository, times(1)).save(any(ArtistModel.class));
    }

    @Test
    void registerArtist_EmailAlreadyExists() {
        ArtistRegistrationDTO registrationDTO = new ArtistRegistrationDTO("testUser", "test@example.com", "password");

        when(artistRepository.existsByEmail(anyString())).thenReturn(true);
        when(artistRepository.existsByName(anyString())).thenReturn(false);

        ResponseEntity<Object> response = artistService.registerArtist(registrationDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) response.getBody();

        assertEquals(1, errors.size());
        assertTrue(errors.contains("Email já está em uso."));
        verify(artistRepository, never()).save(any(ArtistModel.class));
    }

    @Test
    void registerArtist_NameAlreadyExists() {
        ArtistRegistrationDTO registrationDTO = new ArtistRegistrationDTO("testUser", "test@example.com", "password");

        when(artistRepository.existsByEmail(anyString())).thenReturn(false);
        when(artistRepository.existsByName(anyString())).thenReturn(true);

        ResponseEntity<Object> response = artistService.registerArtist(registrationDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) response.getBody();

        assertEquals(1, errors.size());
        assertTrue(errors.contains("Nome de usuário já está em uso."));
        verify(artistRepository, never()).save(any(ArtistModel.class));
    }

    @Test
    void registerArtist_MultipleErrors() {
        ArtistRegistrationDTO registrationDTO = new ArtistRegistrationDTO("testUser", "test@example.com", "password");

        when(artistRepository.existsByEmail(anyString())).thenReturn(true);
        when(artistRepository.existsByName(anyString())).thenReturn(true);

        ResponseEntity<Object> response = artistService.registerArtist(registrationDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) response.getBody();

        assertEquals(2, errors.size());
        assertTrue(errors.contains("Email já está em uso."));
        assertTrue(errors.contains("Nome de usuário já está em uso."));
        verify(artistRepository, never()).save(any(ArtistModel.class));
    }

    @Test
    void findById_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("testUser");
        artist.setEmail("test@example.com");
        artist.setCreationDate(new Date());

        when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));

        ArtistDTO artistDTO = artistService.findById(1L);

        assertEquals("testUser", artistDTO.getName());
        verify(artistRepository, times(1)).findById(anyLong());
    }

    @Test
    void findById_ArtistNotFound() {
        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.findById(1L);
        });
        assertEquals("Artist not found.", exception.getMessage());
    }

    @Test
    void findSongsFromArtist_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Test Artist");

        SongModel song = new SongModel();
        song.setTitle("Test Song");
        song.setArtist(artist);
        artist.setSongs(Set.of(song));

        when(artistRepository.findById(anyLong())).thenReturn(Optional.of(artist));

        List<SongDTO> songs = artistService.findSongsFromArtist(1L);

        assertEquals(1, songs.size());
        assertEquals("Test Song", songs.get(0).getTitle());
        verify(artistRepository, times(1)).findById(anyLong());
    }

    @Test
    void findSongsFromArtist_ArtistNotFound() {
        when(artistRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.findSongsFromArtist(1L);
        });
        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void findArtistFromLoggedUser_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setName("testUser");
        artist.setEmail("test@example.com");

        when(artistRepository.findByName(anyString())).thenReturn(Optional.of(artist));

        ArtistDTO artistDTO = artistService.findArtistFromLoggedUser("testUser");

        assertEquals("testUser", artistDTO.getName());
        verify(artistRepository, times(1)).findByName(anyString());
    }

    @Test
    void findArtistFromLoggedUser_ArtistNotFound() {
        when(artistRepository.findByName(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            artistService.findArtistFromLoggedUser("testUser");
        });
        assertEquals("Artist not found", exception.getMessage());
    }
}
