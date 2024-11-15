package com.soundscape.soundscape.artist;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soundscape.soundscape.artist.dto.ArtistDTO;
import com.soundscape.soundscape.security.JwtTokenUtil;
import com.soundscape.soundscape.song.dto.SongDTO;

@RestController
@RequestMapping("/artists")
public class ArtistController {

	@Autowired
	private ArtistService artistService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@GetMapping(path="/findById")
	public ArtistDTO findById(@RequestParam Long artistId) {
		return artistService.findById(artistId);
	}

	@GetMapping(path="/findSongsFromArtist")
	public List<SongDTO> findSongsFromArtist(@RequestParam Long artistId) {
		return artistService.findSongsFromArtist(artistId);
	}

	@GetMapping(path="/findArtistFromLoggedUser")
	public ArtistDTO findArtistFromLoggedUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		String token = authHeader.substring(7);
	    String userName = jwtTokenUtil.getUsernameFromToken(token);

		return artistService.findArtistFromLoggedUser(userName);
	}

	@PostMapping(path="/followArtist")
	public ResponseEntity<String> followArtist(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, Long artistToFollowId) {
		String token = authHeader.substring(7);
	    String userName = jwtTokenUtil.getUsernameFromToken(token);

	    return artistService.followArtist(userName, artistToFollowId);
	}

//	@GetMapping(path="/findFollowedByUser")
//	public List<ArtistDTO> findFollowedByUser(@RequestParam Long artistId) {
//		return artistService.findArtistsFollowedByUser(artistId);
//	}

	@PostMapping("/unfollowArtist")
    public ResponseEntity<String> unfollowArtist(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam Long artistToUnfollowId) {
		String token = authHeader.substring(7);
	    String userName = jwtTokenUtil.getUsernameFromToken(token);

        return artistService.unfollowArtist(userName, artistToUnfollowId);
    }

	@GetMapping(path="/findFollowersOfUser")
	public List<ArtistDTO> findFollowersOfUser(@RequestParam Long artistId) {
		return artistService.findFollowersOfUser(artistId);
	}

}
