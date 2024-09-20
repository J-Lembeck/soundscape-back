package com.soundscape.soundscape.audiofile;

import java.util.Date;

import com.soundscape.soundscape.song.SongModel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audio_files")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioFileModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;
    private Date creationDate;

    private Long size;

    @OneToOne(mappedBy = "audioFile")
	private SongModel song;
}
