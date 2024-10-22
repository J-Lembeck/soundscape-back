package com.soundscape.soundscape.song.dto;

import java.util.Date;

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
	private Long likes;
	private ArtistDTO artist;
	private Date creationDate;
	private Long length;
	private Boolean isLiked;

    public SongDTO(Long id, String title, ArtistDTO artist, Date creationDate, Long length) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.creationDate = creationDate;
        this.length = length;
    }

}
