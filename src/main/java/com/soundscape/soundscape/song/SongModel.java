package com.soundscape.soundscape.song;

import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.audiofile.AudioFileModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private Long length;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "audio_file_id", referencedColumnName = "id")
	private AudioFileModel audioFile;

	@ManyToOne
	@JoinColumn(name = "artist_id")
	private ArtistModel artist;

	@Lob
    private byte[] imageData;
}
