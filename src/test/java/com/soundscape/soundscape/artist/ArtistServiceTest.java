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

    @Test
    void followArtist_Success() {
        ArtistModel follower = new ArtistModel();
        follower.setName("testUser");

        ArtistModel artistToFollow = new ArtistModel();
        artistToFollow.setId(1L);
        artistToFollow.setName("ArtistToFollow");

        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(follower));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artistToFollow));

        ResponseEntity<String> response = artistService.followArtist("testUser", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Artista seguido com sucesso.", response.getBody());
        assertTrue(artistToFollow.getFollowers().contains(follower));
        verify(artistRepository, times(1)).save(artistToFollow);
    }

    @Test
    void followArtist_AlreadyFollowing() {
        ArtistModel follower = new ArtistModel();
        follower.setName("testUser");

        ArtistModel artistToFollow = new ArtistModel();
        artistToFollow.setId(1L);
        artistToFollow.setName("ArtistToFollow");
        artistToFollow.setFollowers(Set.of(follower));

        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(follower));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artistToFollow));

        ResponseEntity<String> response = artistService.followArtist("testUser", 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Você já segue este artista.", response.getBody());
        verify(artistRepository, never()).save(artistToFollow);
    }

    @Test
    void followArtist_InternalServerError() {
        when(artistRepository.findByName("testUser")).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<String> response = artistService.followArtist("testUser", 1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro ao seguir o artista.", response.getBody());
        verify(artistRepository, times(1)).findByName("testUser");
        verify(artistRepository, never()).save(any());
    }

    @Test
    void unfollowArtist_Success() {
        ArtistModel follower = new ArtistModel();
        follower.setName("testUser");

        ArtistModel artistToUnfollow = new ArtistModel();
        artistToUnfollow.setId(1L);
        artistToUnfollow.setName("ArtistToUnfollow");
        artistToUnfollow.setFollowers(new HashSet<>(Set.of(follower)));

        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(follower));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artistToUnfollow));

        ResponseEntity<String> response = artistService.unfollowArtist("testUser", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Você deixou de seguir o artista com sucesso.", response.getBody());
        assertTrue(artistToUnfollow.getFollowers().isEmpty());
        verify(artistRepository, times(1)).save(artistToUnfollow);
    }

    @Test
    void unfollowArtist_NotFollowing() {
        ArtistModel follower = new ArtistModel();
        follower.setName("testUser");

        ArtistModel artistToUnfollow = new ArtistModel();
        artistToUnfollow.setId(1L);
        artistToUnfollow.setName("ArtistToUnfollow");

        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(follower));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artistToUnfollow));

        ResponseEntity<String> response = artistService.unfollowArtist("testUser", 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Você não segue este artista.", response.getBody());
        verify(artistRepository, never()).save(artistToUnfollow);
    }

    @Test
    void unfollowArtist_InternalServerError() {
        when(artistRepository.findByName("testUser")).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<String> response = artistService.unfollowArtist("testUser", 1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro ao deixar de seguir o artista.", response.getBody());
        verify(artistRepository, times(1)).findByName("testUser");
        verify(artistRepository, never()).save(any());
    }

    @Test
    void findArtistsFollowedByUser_Success() {
        ArtistModel follower = new ArtistModel();
        follower.setName("testUser");

        ArtistModel followedArtist = new ArtistModel();
        followedArtist.setId(1L);
        followedArtist.setName("FollowedArtist");
        followedArtist.setFollowers(Set.of(follower));

        when(artistRepository.findByName("testUser")).thenReturn(Optional.of(follower));
        when(artistRepository.findAll()).thenReturn(List.of(followedArtist));

        List<ArtistDTO> followedArtists = artistService.findArtistsFollowedByUser("testUser");

        assertEquals(1, followedArtists.size());
        assertEquals("FollowedArtist", followedArtists.get(0).getName());
        verify(artistRepository, times(1)).findAll();
    }

    @Test
    void findFollowersOfUser_Success() {
        ArtistModel artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Artist");

        ArtistModel follower = new ArtistModel();
        follower.setName("Follower");
        artist.setFollowers(Set.of(follower));

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        List<ArtistDTO> followers = artistService.findFollowersOfUser(1L);

        assertEquals(1, followers.size());
        assertEquals("Follower", followers.get(0).getName());
        verify(artistRepository, times(1)).findById(1L);
    }

}
