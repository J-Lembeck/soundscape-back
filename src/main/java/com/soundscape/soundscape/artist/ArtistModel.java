package com.soundscape.soundscape.artist;

import java.util.Set;

import com.soundscape.soundscape.song.SongModel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artists")
@AllArgsConstructor
@NoArgsConstructor
public class ArtistModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String password;

	@ManyToMany(mappedBy = "artists")
    private Set<SongModel> songs;
}
