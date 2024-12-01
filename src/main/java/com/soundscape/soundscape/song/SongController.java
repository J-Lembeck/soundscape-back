package com.soundscape.soundscape.song;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.soundscape.soundscape.security.JwtTokenUtil;
import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;

@RestController
@RequestMapping("/songs")
public class SongController {

	@Autowired
	private SongService songService;

	@Autowired
    private JwtTokenUtil jwtTokenUtil;

	@GetMapping(path = "/load/listAll")
	public List<SongDTO> listAllOrForLoggedUser(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
	    if (authHeader != null && authHeader.startsWith("Bearer ")) {
	        String token = authHeader.substring(7);

	        if (jwtTokenUtil.simpleValidateToken(token)) {
	            String userName = jwtTokenUtil.getUsernameFromToken(token);
	            return songService.listAllForLoggedUser(userName);
	        }
	    }
	    return songService.listAll();
	}

	@GetMapping(value = "/load/image", produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
    public byte[] getSongImage(@RequestParam Long id) {
        return songService.getSongImage(id);
    }

	@PostMapping(path = "/upload")
    public ResponseEntity<String> uploadSong(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @ModelAttribute SongUploadDTO songData) throws IOException {
        String token = authHeader.substring(7);
        String userName = jwtTokenUtil.getUsernameFromToken(token);

        return songService.saveSongWithAudio(userName, songData);
    }

	@GetMapping(path = "/searchSongs")
	public List<SongDTO> searchSongs(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam String searchTerm) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
	        String token = authHeader.substring(7);
	        String userName = jwtTokenUtil.getUsernameFromToken(token);

	        return songService.searchSongsForLoggedUser(searchTerm, userName);
	    } else {
	    	return songService.searchSongs(searchTerm);
	    }
	}

	@GetMapping(path="/findLikedSongs")
	public List<SongDTO> findLikedSongsFromLoggedUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		 String token = authHeader.substring(7);
	     String userName = jwtTokenUtil.getUsernameFromToken(token);
	     
	     return songService.findLikedSongsFromArtist(userName);
	}

	@PutMapping(path="/likeSong")
	public ResponseEntity<String> findLikedSongsFromLoggedUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam Long songId) {
		 String token = authHeader.substring(7);
	     String userName = jwtTokenUtil.getUsernameFromToken(token);
	     
	     return songService.likeSong(userName, songId);
	}

	@GetMapping(path="/downloadSong")
	public ResponseEntity<byte[]> downloadSong(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam Long songId) {
		 String token = authHeader.substring(7);
	     String userName = jwtTokenUtil.getUsernameFromToken(token);

	     return songService.downloadAudioFile(userName, songId);
	}
}
