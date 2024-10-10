package com.soundscape.soundscape.song.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongImageRepository extends JpaRepository<SongImageModel, Long>{

	@Query("SELECT si.imageData FROM SongImageModel si JOIN si.song s WHERE s.id = :songId")
	byte[] findImageBySongId(@Param("songId") Long songId);
}
