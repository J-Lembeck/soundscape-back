package com.soundscape.soundscape.artist;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

	@Transactional
	public ResponseEntity<String> followArtist(String userName, Long artistToFollowId) {
		try {
			ArtistModel follower = artistRepository.findByName(userName)
				.orElseThrow(() -> new IllegalArgumentException("Artista não encontrado com o email: " + userName));

			ArtistModel artistToFollow = artistRepository.findById(artistToFollowId)
				.orElseThrow(() -> new IllegalArgumentException("Artista não encontrado com o ID: " + artistToFollowId));

			if (artistToFollow.getFollowers().contains(follower)) {
				return ResponseEntity.badRequest().body("Você já segue este artista.");
			}

			artistToFollow.addFollower(follower);

			return ResponseEntity.ok("Artista seguido com sucesso.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro ao seguir o artista.");
		}
	}

	public void addFollower(ArtistModel follower) {
		this.followers.add(follower);
		follower.following.add(this);
	}

	@Transactional
	public ResponseEntity<String> unfollowArtist(String userName, Long artistToUnfollowId) {
	    try {
	        ArtistModel follower = artistRepository.findByName(userName)
	            .orElseThrow(() -> new IllegalArgumentException("Artista não encontrado com o nome: " + userName));

	        ArtistModel artistToUnfollow = artistRepository.findById(artistToUnfollowId)
	            .orElseThrow(() -> new IllegalArgumentException("Artista não encontrado com o ID: " + artistToUnfollowId));

	        if (!artistToUnfollow.getFollowers().contains(follower)) {
	            return ResponseEntity.badRequest().body("Você não segue este artista.");
	        }

	        artistToUnfollow.removeFollower(follower);

	        return ResponseEntity.ok("Você deixou de seguir o artista com sucesso.");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body("Ocorreu um erro ao deixar de seguir o artista.");
	    }
	}

	public void removeFollower(ArtistModel follower) {
		this.followers.remove(follower);
		follower.following.remove(this);
	}

//	@Transactional
//	public List<ArtistDTO> findArtistsFollowedByUser(Long artistId) {
//	    ArtistModel artist = artistRepository.findById(artistId)
//	            .orElseThrow(() -> new IllegalArgumentException("Artist not found."));
//
//	    return artist.getFollowing().stream()
//	            .map(followedArtist -> new ArtistFactory().buildDTO(followedArtist))
//	            .collect(Collectors.toList());
//	}

	@Transactional
	public List<ArtistDTO> findFollowersOfUser(Long artistId) {
	    ArtistModel artist = artistRepository.findById(artistId)
	            .orElseThrow(() -> new IllegalArgumentException("Artist not found."));

	    return artist.getFollowers().stream()
	            .map(follower -> new ArtistFactory().buildDTO(follower))
	            .collect(Collectors.toList());
	}


}
