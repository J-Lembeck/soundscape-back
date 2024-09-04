package com.soundscape.soundscape.artist.dto;

import com.soundscape.soundscape.artist.ArtistModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistDTO {

	private Long id;
	private String name;

	public ArtistDTO(ArtistModel model) {
		this.id = model.getId();
		this.name = model.getName();
	}
}
