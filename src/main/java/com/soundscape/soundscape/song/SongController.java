package com.soundscape.soundscape.song;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;

@RestController
@RequestMapping("/songs")
public class SongController {

	@Autowired
	private SongService songService;

	@GetMapping(path = "/load/listAll")
	public List<SongDTO> listAll() {
		return songService.listAll();
	}

	@GetMapping(value = "/load/image", produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
    public byte[] getSongImage(@RequestParam Long id) {
        return songService.getSongImage(id);
    }

	@PostMapping(path = "/upload")
	public ResponseEntity<String> uploadSong(@RequestParam Long artistId, @ModelAttribute SongUploadDTO songData) throws IOException{
		return songService.saveSongWithAudio(artistId, songData);
	}
}
