package com.soundscape.soundscape.artist;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.song.SongFactory;
import com.soundscape.soundscape.song.dto.SongDTO;

import jakarta.transaction.Transactional;

@Service
public class ArtistService {

	@Autowired
	private ArtistRepository artistRepository;

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
