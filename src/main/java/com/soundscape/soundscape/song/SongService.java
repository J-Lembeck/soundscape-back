package com.soundscape.soundscape.song;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.acrcloud.utils.ACRCloudRecognizer;
import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.artist.ArtistModel;
import com.soundscape.soundscape.artist.ArtistRepository;
import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.audiofile.AudioFileRepository;
import com.soundscape.soundscape.song.dto.SongDTO;
import com.soundscape.soundscape.song.dto.SongUploadDTO;
import com.soundscape.soundscape.song.image.SongImageModel;
import com.soundscape.soundscape.song.image.SongImageRepository;

import jakarta.transaction.Transactional;

@Service
public class SongService {

	
	@Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AudioFileRepository audioFileRepository;

    @Autowired
    private SongImageRepository songImageRepository;

    private String accessKey = System.getenv("ACR_ACCESS_KEY");
    private String accessSecret = System.getenv("ACR_ACCESS_SECRET");
    private String acrHost = System.getenv("ACR_HOST");

    public ResponseEntity<String> saveSongWithAudio(String userName, SongUploadDTO songData) throws IOException {
    	ArtistModel artist = artistRepository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        try {
            Date currentDate = new Date();

            String originalAudioFileName = songData.getAudioFile().getOriginalFilename();
            String audioFileExtension = originalAudioFileName.substring(originalAudioFileName.lastIndexOf("."));
            String uniqueAudioFileName = UUID.randomUUID().toString() + audioFileExtension;

            byte[] audioData = songData.getAudioFile().getBytes();

            Map<String, Object> config = new HashMap<>();
            config.put("host", acrHost);
            config.put("access_key", accessKey);
            config.put("access_secret", accessSecret);
            config.put("timeout", 10);

            ACRCloudRecognizer recognizer = new ACRCloudRecognizer(config);

            String result = recognizer.recognizeByFileBuffer(audioData, audioData.length, 0);

            JSONObject resultJson = new JSONObject(result);
            int statusCode = resultJson.getJSONObject("status").getInt("code");

            if (statusCode == 0) {
                JSONObject metadata = resultJson.getJSONObject("metadata");
                JSONArray musicArray = metadata.getJSONArray("music");
                JSONObject firstMatch = musicArray.getJSONObject(0);

                String matchedTitle = firstMatch.getString("title");
                String matchedArtist = firstMatch.getJSONArray("artists").getJSONObject(0).getString("name");

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Upload failed: The song matches a copyrighted song: " + matchedTitle + " by " + matchedArtist);
            } else {
                AudioFileModel audioFileModel = new AudioFileModel();
                audioFileModel.setFileName(uniqueAudioFileName);
                audioFileModel.setFileData(IOUtils.toByteArray(songData.getAudioFile().getInputStream()));
                audioFileModel.setSize(songData.getAudioFile().getSize());
                audioFileModel.setCreationDate(currentDate);

                audioFileRepository.save(audioFileModel);

                File tempAudioFile = File.createTempFile("tempAudio", audioFileExtension);
                try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                    fos.write(audioData);
                }

                Mp3File mp3File = new Mp3File(tempAudioFile);
                long durationInSeconds = mp3File.getLengthInSeconds();

                SongModel songModel = new SongModel();
                songModel.setTitle(songData.getTitle());
                songModel.setAudioFile(audioFileModel);
                songModel.setArtist(artist);
                songModel.setLength(durationInSeconds);
                songModel.setLikes(0L);

                if (songData.getImageFile() != null) {
                    byte[] imageData = IOUtils.toByteArray(songData.getImageFile().getInputStream());
                    SongImageModel songImageModel = songImageRepository.save(new SongImageModel(imageData, currentDate));
                    songModel.setSongImage(songImageModel);
                }
                songModel.setCreationDate(currentDate);

                songRepository.save(songModel);
                tempAudioFile.delete();

                return ResponseEntity.ok("Song and image uploaded successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload song and image: " + e.getMessage());
        }
    }

    @Transactional
    public byte[] getSongImage(Long songId) {
    	return this.songImageRepository.findImageBySongId(songId);
    }

    public List<SongDTO> listAllForLoggedUser(String userName) {
    	ArtistModel artist = artistRepository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

    	return this.songRepository.findAllWithoutImageDataAndLikedStatus(artist.getId());
    }

    public List<SongDTO> listAll() {
    	return this.songRepository.findAllWithoutImageDataOrderByCreationDate();
    }

    @Transactional
    public List<SongDTO> searchSongsForLoggedUser(String searchTerm, String username) {
    	ArtistModel artist = artistRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        List<SongDTO> foundSongs = songRepository.searchByTitleOrArtistNameAndLiked(searchTerm, artist.getId());
        return foundSongs;
    }

    @Transactional
    public List<SongDTO> searchSongs(String searchTerm) {
        List<SongModel> foundSongs = songRepository.searchByTitleOrArtistName(searchTerm);
        
        return foundSongs.stream().map(song -> new SongFactory().buildDTO(song))
                .collect(Collectors.toList());
    }

    public List<SongDTO> findLikedSongsFromArtist(String userName) {
    	ArtistModel artist = artistRepository.findByName(userName)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

    	return artist.getLikedSongs().stream().map(song -> new SongFactory().buildLikedDTO(song))
                .collect(Collectors.toList());
    }

    public ResponseEntity<String> likeSong(String userName, Long songId) {
        try {
            ArtistModel artist = artistRepository.findByName(userName)
                    .orElseThrow(() -> new IllegalArgumentException("Artist not found."));

            SongModel song = songRepository.findById(songId)
                    .orElseThrow(() -> new IllegalArgumentException("Song not found."));

            Set<SongModel> likedSongs = artist.getLikedSongs();

            if (likedSongs.contains(song)) {
                likedSongs.remove(song);
                song.setLikes(Long.sum(song.getLikes(), -1L));
                songRepository.save(song);
                artistRepository.save(artist);
                return ResponseEntity.ok("Song removed from liked songs successfully.");
            } else {
                likedSongs.add(song);
                song.setLikes(Long.sum(song.getLikes(), 1L));
                songRepository.save(song);
                artistRepository.save(artist);
                return ResponseEntity.ok("Song added to liked songs successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao processar a ação: " + e.getMessage());
        }
    }
}
