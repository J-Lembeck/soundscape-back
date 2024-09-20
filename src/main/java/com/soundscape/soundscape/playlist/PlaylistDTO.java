package com.soundscape.soundscape.playlist;

import java.util.Date;

import lombok.Data;

@Data
public class PlaylistDTO {

	private Long id;
	private String name;
	private Date creationDate;
}
