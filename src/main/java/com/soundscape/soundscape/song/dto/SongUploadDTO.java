package com.soundscape.soundscape.song.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongUploadDTO {

	private String title;
	private MultipartFile audioFile;
	private MultipartFile imageFile;

}
