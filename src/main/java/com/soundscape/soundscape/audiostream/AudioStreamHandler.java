package com.soundscape.soundscape.audiostream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.s3.S3Service;
import com.soundscape.soundscape.song.SongRepository;

import jakarta.transaction.Transactional;
import software.amazon.awssdk.regions.Region;

@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

    private Long songId;
    private WebSocketSession currentSession;

    @Autowired
    private SongRepository songRepository;

    private String s3AccessKeyID = System.getenv("AWS_ACCESS_KEY_ID");
    private String s3AccessKeySecret = System.getenv("AWS_ACCESS_KEY_SECRET");

    public AudioStreamHandler(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    protected S3Service createS3Service() {
        return new S3Service(s3AccessKeyID, s3AccessKeySecret, Region.US_EAST_2);
    }

    protected InputStream openStream(String url) throws IOException {
        return new URL(url).openStream();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        if (payload.startsWith("songId:")) {
            if (currentSession != null && currentSession.isOpen() && !currentSession.equals(session)) {
                try {
                    currentSession.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            songId = Long.parseLong(payload.split(":")[1]);
            currentSession = session;

            try {
                startStreaming(session);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional
    private void startStreaming(WebSocketSession session) throws Exception {
        String s3Key = songRepository.findSongWithAudioFileById(songId)
            .map(song -> song.getAudioFile().getFilePath())
            .orElse(null);

        if (s3Key == null) {
            session.sendMessage(new TextMessage("Error: Audio file not found."));
            return;
        }

        String bucketName = "soundscape-files";

        S3Service s3Service = createS3Service();
        String presignedUrl = s3Service.generatePresignedUrl(bucketName, s3Key);

        try (InputStream inputStream = openStream(presignedUrl)) {
            byte[] audioData = inputStream.readAllBytes();

            if (audioData.length == 0) {
                session.sendMessage(new TextMessage("Error: Downloaded audio file is empty."));
                return;
            }

            File tempAudioFile = File.createTempFile("tempAudio", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                fos.write(audioData);
            }

            try {
                Mp3File mp3file = new Mp3File(tempAudioFile);
                long durationInSeconds = mp3file.getLengthInSeconds();
                session.sendMessage(new TextMessage("duration:" + durationInSeconds));
            } catch (InvalidDataException e) {
                session.sendMessage(new TextMessage("Error: Invalid MP3 data."));
                e.printStackTrace();
                return;
            } finally {
                tempAudioFile.delete();
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(audioData);
            int chunkSize = 1024;
            while (byteBuffer.hasRemaining()) {
                byte[] buffer = new byte[Math.min(chunkSize, byteBuffer.remaining())];
                byteBuffer.get(buffer);
                session.sendMessage(new BinaryMessage(ByteBuffer.wrap(buffer)));
            }
        }
    }

    protected InputStream downloadAudioFromS3(String audioFilePath) throws IOException {
        URL s3Url = new URL(audioFilePath);
        return s3Url.openStream();
    }
}
