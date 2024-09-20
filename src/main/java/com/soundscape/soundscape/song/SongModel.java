package com.soundscape.soundscape.song;
import java.util.Date;

import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.audiofile.AudioFileModel;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    private Date creationDate;

    @OneToOne
    @JoinColumn(name = "audio_file_id", referencedColumnName = "id")
    private AudioFileModel audioFile;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private ArtistModel artist;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] imageData;
}
