package com.soundscape.soundscape.playlist;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;
import com.soundscape.soundscape.song.SongFactory;
import com.soundscape.soundscape.song.SongModel;
import com.soundscape.soundscape.song.SongRepository;
import com.soundscape.soundscape.song.dto.SongDTO;

import jakarta.transaction.Transactional;

@Service
public class PlaylistService {

	@Autowired
	private PlaylistRepository playlistRepository;

	@Autowired
	private ArtistRepository artistRepository;

	@Autowired
	private SongRepository songRepository;

	public List<PlaylistDTO> findAllByArtist(String userName) {
		ArtistModel artist = artistRepository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

		return this.playlistRepository.findAllByArtistId(artist.getId()).stream()
				.map(playlist -> new PlaylistFactory().buildBean(playlist)).collect(Collectors.toList());
	}

	public PlaylistDTO createNewPlaylist(String userName, String playlistName) {
        ArtistModel artist = artistRepository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));
        Date currentDate = new Date();

		PlaylistModel newPlaylist = new PlaylistModel();

		newPlaylist.setArtist(artist);
		newPlaylist.setName(playlistName);
		newPlaylist.setCreationDate(currentDate);

		newPlaylist = this.playlistRepository.save(newPlaylist);

		return new PlaylistFactory().buildBean(newPlaylist);
	}

	@Transactional
	public List<SongDTO> findSongsFromPlaylist(Long playlistId) {
		PlaylistModel playlist = this.playlistRepository.findById(playlistId)
				.orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

		return playlist.getSongs().stream().map(song -> new SongFactory().buildDTO(song)).collect(Collectors.toList());
	}

	@Transactional
	public ResponseEntity<String> addSongToPlaylist(Long playlistId, Long songId, String userName) {
		ArtistModel artistLogged = artistRepository.findByName(userName)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

	    PlaylistModel playlist = playlistRepository.findById(playlistId)
	            .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

	    if (!artistLogged.getId().equals(playlist.getArtist().getId())) {
	        return ResponseEntity.badRequest().body("No permission to edit this playlist.");
	    }
	    
	    SongModel songToAdd = songRepository.findById(songId)
	            .orElseThrow(() -> new IllegalArgumentException("Song not found"));

	    if (playlist.getSongs().contains(songToAdd)) {
	        return ResponseEntity.badRequest().body("The song is already in the playlist.");
	    }

	    playlist.getSongs().add(songToAdd);
	    playlistRepository.save(playlist);

	    return ResponseEntity.ok("Song added to the playlist successfully.");
	}

	@Transactional
	public ResponseEntity<String> removeSongFromPlaylist(Long playlistId, Long songId, String userName) {
		ArtistModel artistLogged = artistRepository.findByName(userName)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

	    PlaylistModel playlist = playlistRepository.findById(playlistId)
	            .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

	    if (!artistLogged.getId().equals(playlist.getArtist().getId())) {
	        return ResponseEntity.badRequest().body("No permission to edit this playlist.");
	    }

	    SongModel songToRemove = songRepository.findById(songId)
	            .orElseThrow(() -> new IllegalArgumentException("Song not found"));

	    if (!playlist.getSongs().contains(songToRemove)) {
	        return ResponseEntity.badRequest().body("The song is not in the playlist.");
	    }

	    playlist.getSongs().remove(songToRemove);
	    playlistRepository.save(playlist);

	    return ResponseEntity.ok("Song removed from the playlist successfully.");
	}

	public ResponseEntity<String> deletePlaylist(Long playlistId, String userName) {
		try {
			ArtistModel artistLogged = artistRepository.findByName(userName)
					.orElseThrow(() -> new IllegalArgumentException("User not found"));

			PlaylistModel playlist = playlistRepository.findById(playlistId)
					.orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

			if (!artistLogged.getId().equals(playlist.getArtist().getId())) {
		        return ResponseEntity.badRequest().body("No permission to delete this playlist.");
		    }

			playlistRepository.deleteById(playlistId);
			return ResponseEntity.ok("Playlist deleted.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok("An error ocurred.");
		}
		
	}
}
