package com.soundscape.soundscape.artist;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<String> registerArtist(ArtistRegistrationDTO registrationDTO) {
        if (artistRepository.existsByEmail(registrationDTO.getEmail())) {
            return ResponseEntity.status(409).body("Email já está em uso.");
        }

        if (artistRepository.existsByName(registrationDTO.getName())) {
            return ResponseEntity.status(409).body("Nome de usuário já está em uso.");
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
}
