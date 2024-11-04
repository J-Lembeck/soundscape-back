package com.soundscape.soundscape.song;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soundscape.soundscape.song.dto.SongDTO;

public interface SongRepository extends JpaRepository<SongModel, Long>{

	@Query("SELECT new com.soundscape.soundscape.song.dto.SongDTO(s.id, s.title, new com.soundscape.soundscape.artist.dto.ArtistDTO(a.id, a.name), s.creationDate, s.length) "
			+ "       FROM SongModel s JOIN s.artist a ORDER BY s.creationDate DESC")
	public List<SongDTO> findAllWithoutImageDataOrderByCreationDate();

	@Query("SELECT new com.soundscape.soundscape.song.dto.SongDTO(s.id, s.title, s.likes, new com.soundscape.soundscape.artist.dto.ArtistDTO(a.id, a.name), s.creationDate, s.length, " +
		       "CASE WHEN (s IN (SELECT song FROM ArtistModel artist JOIN artist.likedSongs song WHERE artist.id = :artistId)) THEN true ELSE false END) " +
		       "FROM SongModel s " +
		       "JOIN s.artist a " +
		       "ORDER BY s.creationDate DESC")
	public List<SongDTO> findAllWithoutImageDataAndLikedStatus(@Param("artistId") Long artistId);

	@Query("SELECT new com.soundscape.soundscape.song.dto.SongDTO(s.id, s.title, s.likes, new com.soundscape.soundscape.artist.dto.ArtistDTO(a.id, a.name), s.creationDate, s.length, " +
		       "CASE WHEN (s IN (SELECT song FROM ArtistModel artist JOIN artist.likedSongs song WHERE artist.id = :artistId)) THEN true ELSE false END) " +
		       "FROM SongModel s " +
		       "JOIN s.artist a " +
		       "WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
		       "OR LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
		       "ORDER BY s.creationDate DESC")
		public List<SongDTO> searchByTitleOrArtistNameAndLiked(@Param("searchTerm") String searchTerm, @Param("artistId") Long artistId);

    @Query("SELECT s FROM SongModel s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    public List<SongModel> searchByTitleOrArtistName(@Param("searchTerm") String searchTerm);

	@Query("SELECT s FROM SongModel s JOIN FETCH s.audioFile WHERE s.id = :songId")
    Optional<SongModel> findSongWithAudioFileById(@Param("songId") Long songId);
}
