package com.soundscape.soundscape.artist;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.soundscape.soundscape.song.SongModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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

	@OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<SongModel> songs = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
	    name = "artists_liked_songs",
	    joinColumns = @JoinColumn(name = "artist_id"),
	    inverseJoinColumns = @JoinColumn(name = "song_id")
	)
	private Set<SongModel> likedSongs = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "artist_followers",
        joinColumns = @JoinColumn(name = "artist_id"),
        inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    @JsonManagedReference
    private Set<ArtistModel> followers = new HashSet<>();

	@ManyToMany(mappedBy = "followers", fetch = FetchType.LAZY)
	private Set<ArtistModel> following = new HashSet<>();

	public void addFollower(ArtistModel follower) {
		this.followers.add(follower);
		follower.following.add(this);
	}

	public void removeFollower(ArtistModel follower) {
		this.followers.remove(follower);
		follower.following.remove(this);
	}
}
