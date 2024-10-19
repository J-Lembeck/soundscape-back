package com.soundscape.soundscape.artist;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.soundscape.soundscape.song.SongModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artists")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String email;
	private String password;
	private Date creationDate;

	@OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<SongModel> songs = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
	    name = "artists_liked_songs",
	    joinColumns = @JoinColumn(name = "artist_id"),
	    inverseJoinColumns = @JoinColumn(name = "song_id")
	)
	private Set<SongModel> likedSongs = new HashSet<>();
}
