package com.soundscape.soundscape.song;

import com.soundscape.soundscape.audiofile.AudioFileModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@AllArgsConstructor
@NoArgsConstructor
public class SongModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;

	@OneToOne(mappedBy = "song", cascade = CascadeType.ALL)
	private AudioFileModel audioFile;

}
