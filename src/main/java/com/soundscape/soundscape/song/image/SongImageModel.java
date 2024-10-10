package com.soundscape.soundscape.song.image;

import java.util.Date;

import com.soundscape.soundscape.song.SongModel;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "song_image")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SongImageModel {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
	private Date creationDate;

    @OneToOne(mappedBy = "songImage")
    private SongModel song;

    @Basic(fetch = FetchType.EAGER)
    private byte[] imageData;

	public SongImageModel(byte[] imageData, Date creationDate) {
		this.imageData = imageData;
		this.creationDate = creationDate;
	}
}
