package com.soundscape.soundscape.playlist;

public class PlaylistFactory {

	public PlaylistDTO buildBean(PlaylistModel playlistModel) {
		PlaylistDTO playlistDTO = new PlaylistDTO();

		playlistDTO.setId(playlistModel.getId());
		playlistDTO.setName(playlistModel.getName());
		playlistDTO.setCreationDate(playlistModel.getCreationDate());

		return playlistDTO;
	}
}
