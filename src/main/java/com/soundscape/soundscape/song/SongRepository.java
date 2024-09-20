package com.soundscape.soundscape.song;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soundscape.soundscape.song.dto.SongDTO;

public interface SongRepository extends JpaRepository<SongModel, Long>{

	@Query("SELECT new com.soundscape.soundscape.song.dto.SongDTO(s.id, s.title, new com.soundscape.soundscape.artist.dto.ArtistDTO(a.id, a.name), s.creationDate, s.length) "
			+ "       FROM SongModel s JOIN s.artist a ORDER BY s.creationDate DESC")
	List<SongDTO> findAllWithoutImageDataOrderByCreationDate();

    @Query("SELECT s FROM SongModel s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<SongModel> searchByTitleOrArtistName(@Param("searchTerm") String searchTerm);

}
