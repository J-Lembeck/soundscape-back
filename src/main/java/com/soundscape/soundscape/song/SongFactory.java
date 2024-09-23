package com.soundscape.soundscape.song;

import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.song.dto.SongDTO;

public class SongFactory {

	public SongDTO buildDTO(SongModel model) {
		SongDTO dto = new SongDTO();

		dto.setId(model.getId());
		dto.setTitle(model.getTitle());
		dto.setLength(model.getLength());
		dto.setArtist(new ArtistDTO(model.getArtist()));
		dto.setCreationDate(model.getCreationDate());

		return dto;
	}
}
