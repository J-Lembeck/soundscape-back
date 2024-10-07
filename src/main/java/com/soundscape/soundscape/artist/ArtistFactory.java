package com.soundscape.soundscape.artist;

import com.soundscape.soundscape.artist.dto.ArtistDTO;

public class ArtistFactory {

	public ArtistDTO buildDTO(ArtistModel model) {
		ArtistDTO artistDTO = new ArtistDTO();

		artistDTO.setId(model.getId());
		artistDTO.setName(model.getName());

		return artistDTO;
	}
}
