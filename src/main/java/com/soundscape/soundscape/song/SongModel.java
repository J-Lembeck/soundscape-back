package com.soundscape.soundscape.song;
import java.util.Date;

import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.song.image.SongImageModel;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SongModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    private String title;
    private Long length;
    private Long likes;
    private Date creationDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "audio_file_id", referencedColumnName = "id")
    private AudioFileModel audioFile;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private ArtistModel artist;

    @OneToOne
    @JoinColumn(name = "song_image_id", referencedColumnName = "id")
    private SongImageModel songImage;
}
