package com.soundscape.soundscape.artist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.artist.dto.ArtistRegistrationDTO;
import com.soundscape.soundscape.song.SongFactory;
import com.soundscape.soundscape.song.dto.SongDTO;

import jakarta.transaction.Transactional;

@Service
public class ArtistService {

	@Autowired
	private ArtistRepository artistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ARTIST_NOT_FOUND_BY_NAME = "Artista não encontrado com o nome: ";
    private static final String ARTIST_NOT_FOUND_BY_ID = "Artista não encontrado com o ID: ";

    @Validated
    public ResponseEntity<Object> registerArtist(ArtistRegistrationDTO registrationDTO) {
        List<String> errors = new ArrayList<>();

        if (artistRepository.existsByEmail(registrationDTO.getEmail())) {
            errors.add("Email já está em uso.");
        }

        if (artistRepository.existsByName(registrationDTO.getName())) {
            errors.add("Nome de usuário já está em uso.");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        }

        String encodedPassword = passwordEncoder.encode(registrationDTO.getPassword());

        ArtistModel newArtist = new ArtistModel();
        newArtist.setName(registrationDTO.getName());
        newArtist.setEmail(registrationDTO.getEmail());
        newArtist.setPassword(encodedPassword);
        newArtist.setCreationDate(new Date());

        artistRepository.save(newArtist);

        return ResponseEntity.ok("Artista registrado com sucesso.");
    }

	public ArtistDTO findById(Long artistId) {
		ArtistModel model = artistRepository.findById(artistId)
				.orElseThrow(() -> new IllegalArgumentException("Artist not found."));
		
		return new ArtistFactory().buildDTO(model);
	}

	@Transactional
	public List<SongDTO> findSongsFromArtist(Long artistId) {
		ArtistModel artist = artistRepository.findById(artistId)
				.orElseThrow(() -> new IllegalArgumentException("Artist not found"));

		return artist.getSongs().stream().map(song -> new SongFactory().buildDTO(song)).collect(Collectors.toList());
	}

	public ArtistDTO findArtistFromLoggedUser(String userName) {
		ArtistModel artist = artistRepository.findByName(userName)
				.orElseThrow(() -> new IllegalArgumentException("Artist not found"));

		return new ArtistFactory().buildDTO(artist);
	}

	@Transactional
	public ResponseEntity<String> followArtist(String userName, Long artistToFollowId) {
	    try {
	        ArtistModel follower = artistRepository.findByName(userName)
	                .orElseThrow(() -> new IllegalArgumentException(ARTIST_NOT_FOUND_BY_NAME + userName));

	        ArtistModel artistToFollow = artistRepository.findById(artistToFollowId)
	                .orElseThrow(() -> new IllegalArgumentException(ARTIST_NOT_FOUND_BY_ID + artistToFollowId));

	        if (artistToFollow.getFollowers().contains(follower)) {
	            return ResponseEntity.badRequest().body("Você já segue este artista.");
	        }

	        artistToFollow.getFollowers().add(follower);
	        artistRepository.save(artistToFollow);

	        return ResponseEntity.ok("Artista seguido com sucesso.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro ao seguir o artista.");
	    }
	}

	@Transactional
	public ResponseEntity<String> unfollowArtist(String userName, Long artistToUnfollowId) {
	    try {
	        ArtistModel follower = artistRepository.findByName(userName)
	            .orElseThrow(() -> new IllegalArgumentException(ARTIST_NOT_FOUND_BY_NAME + userName));

	        ArtistModel artistToUnfollow = artistRepository.findById(artistToUnfollowId)
	            .orElseThrow(() -> new IllegalArgumentException(ARTIST_NOT_FOUND_BY_ID + artistToUnfollowId));

	        if (!artistToUnfollow.getFollowers().contains(follower)) {
	            return ResponseEntity.badRequest().body("Você não segue este artista.");
	        }

	        artistToUnfollow.getFollowers().remove(follower);
	        artistRepository.save(artistToUnfollow);

	        return ResponseEntity.ok("Você deixou de seguir o artista com sucesso.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body("Ocorreu um erro ao deixar de seguir o artista.");
	    }
	}

	@Transactional
	public List<ArtistDTO> findArtistsFollowedByUser(String userName) {
	    ArtistModel follower = artistRepository.findByName(userName)
	            .orElseThrow(() -> new IllegalArgumentException(ARTIST_NOT_FOUND_BY_NAME + userName));

	    return artistRepository.findAll().stream()
	            .filter(artist -> artist.getFollowers().contains(follower))
	            .map(artist -> new ArtistFactory().buildDTO(artist))
	            .collect(Collectors.toList());
	}

	@Transactional
	public List<ArtistDTO> findFollowersOfUser(Long artistId) {
	    ArtistModel artist = artistRepository.findById(artistId)
	            .orElseThrow(() -> new IllegalArgumentException("Artist not found."));

	    return artist.getFollowers().stream()
	            .map(follower -> new ArtistFactory().buildDTO(follower))
	            .collect(Collectors.toList());
	}

}
