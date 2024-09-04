package com.soundscape.soundscape.song.dto;

import com.soundscape.soundscape.artist.dto.ArtistDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongDTO {

	private Long id;
	private String title;
	private ArtistDTO artist;
	private Long length;

}
