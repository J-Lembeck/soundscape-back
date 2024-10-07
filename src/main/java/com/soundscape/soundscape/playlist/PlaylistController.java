package com.soundscape.soundscape.playlist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soundscape.soundscape.security.JwtTokenUtil;
import com.soundscape.soundscape.song.dto.SongDTO;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

	@Autowired
	private PlaylistService playlistService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@GetMapping(path = "/findAllByLoggedUser")
	public List<PlaylistDTO> findAllByLoggedUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		String token = authHeader.substring(7);
	    String userName = jwtTokenUtil.getUsernameFromToken(token);

		return playlistService.findAllByArtist(userName);
	}

	@PostMapping("/createNewPlaylist")
	public PlaylistDTO createNewPlaylist(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam String playlistName) {
		String token = authHeader.substring(7);
	    String userName = jwtTokenUtil.getUsernameFromToken(token);

	    return playlistService.createNewPlaylist(userName, playlistName);
	}

	@GetMapping(path = "/findSongsFromPlaylist")
	public List<SongDTO> findSongsFromPlaylist(@RequestParam Long playlistId) {
		return playlistService.findSongsFromPlaylist(playlistId);
	}

	@PutMapping(path = "/addSongToPlaylist")
	public ResponseEntity<String> addSongToPlaylist(@RequestParam Long playlistId, @RequestParam Long songId) {
		return playlistService.addSongToPlaylist(playlistId, songId);
	}

	@PutMapping(path = "/removeSongFromPlaylist")
	public ResponseEntity<String> removeSongFromPlaylist(@RequestParam Long playlistId, @RequestParam Long songId) {
		return playlistService.removeSongFromPlaylist(playlistId, songId);
	}

	@DeleteMapping(path = "/deletePlaylist")
	public ResponseEntity<String> deletePlaylist(@RequestParam Long playlistId) {
		return playlistService.deletePlaylist(playlistId);
	}
}
