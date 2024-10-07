package com.soundscape.soundscape.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soundscape.soundscape.artist.dto.ArtistDTO;

@Service
public class ArtistService {

	@Autowired
	private ArtistRepository artistRepository;

	public ArtistDTO findById(Long artistId) {
		ArtistModel model = artistRepository.findById(artistId)
				.orElseThrow(() -> new IllegalArgumentException("Artist not found."));
		
		return new ArtistFactory().buildDTO(model);
	}
}
