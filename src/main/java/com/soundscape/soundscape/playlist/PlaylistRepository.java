package com.soundscape.soundscape.playlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<PlaylistModel, Long>{

	public List<PlaylistModel> findAllByArtistId(Long artistId);
}
