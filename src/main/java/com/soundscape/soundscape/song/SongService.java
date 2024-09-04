package com.soundscape.soundscape.song;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;
import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;

@Service
public class SongService {

	@Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistRepository artistRepository;

    public ResponseEntity<String> saveSongWithAudio(Long artistId, SongUploadDTO songData) throws IOException {
        try {
            ArtistModel artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

            String originalAudioFileName = songData.getAudioFile().getOriginalFilename();
            String audioFileExtension = originalAudioFileName.substring(originalAudioFileName.lastIndexOf("."));
            String uniqueAudioFileName = UUID.randomUUID().toString() + audioFileExtension;
            String audioFilePath = "C:/audioFiles/" + uniqueAudioFileName;

            File audioFile = new File(audioFilePath);
            songData.getAudioFile().transferTo(audioFile);

            AudioFileModel audioFileModel = new AudioFileModel();
            audioFileModel.setFileName(uniqueAudioFileName);
            audioFileModel.setFilePath(audioFilePath);
            audioFileModel.setSize(songData.getAudioFile().getSize());

            Mp3File mp3File = new Mp3File(audioFilePath);
            long durationInSeconds = mp3File.getLengthInSeconds();

            byte[] imageData = IOUtils.toByteArray(songData.getImageFile().getInputStream());

            SongModel songModel = new SongModel();
            songModel.setTitle(songData.getTitle());
            songModel.setAudioFile(audioFileModel);
            songModel.setArtist(artist);
            songModel.setLength(durationInSeconds);
            songModel.setImageData(imageData);

            songRepository.save(songModel);

            return ResponseEntity.ok("Song and image uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload song and image: " + e.getMessage());
        }
    }
    
    public byte[] getSongImage(Long songId) {
    	return this.songRepository.findById(songId).get().getImageData();
    }

    public List<SongDTO> listAll() {
    	return this.songRepository.findAll().stream().map(song -> new SongFactory().buildDTO(song)).toList();
    }
}
