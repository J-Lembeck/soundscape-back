package com.soundscape.soundscape.artist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soundscape.soundscape.artist.dto.ArtistDTO;

@RestController
@RequestMapping("/artists")
public class ArtistController {

	@Autowired
	private ArtistService artistService;

	@GetMapping(path="/findById")
	public ArtistDTO findById(@RequestParam Long artistId) {
		return artistService.findById(artistId);
	}
}
