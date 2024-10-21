package com.soundscape.soundscape.audiostream;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.song.SongRepository;

import jakarta.transaction.Transactional;

@Component
public class AudioStreamHandler extends BinaryWebSocketHandler {

    private Long songId;
    private WebSocketSession currentSession;

    @Autowired
    private SongRepository songRepository;

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
        byte[] audioData = getAudioDataBySongId(songId);

        if (audioData == null) {
            session.sendMessage(new TextMessage("Error: Audio file not found."));
            return;
        }

        File tempAudioFile = File.createTempFile("tempAudio", ".mp3");
        try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
            fos.write(audioData);
        }

        Mp3File mp3file = new Mp3File(tempAudioFile);
        long durationInSeconds = mp3file.getLengthInSeconds();
        session.sendMessage(new TextMessage("duration:" + durationInSeconds));

        ByteBuffer byteBuffer = ByteBuffer.wrap(audioData);
        int chunkSize = 1024;
        while (byteBuffer.hasRemaining()) {
            byte[] buffer = new byte[Math.min(chunkSize, byteBuffer.remaining())];
            byteBuffer.get(buffer);
            session.sendMessage(new BinaryMessage(ByteBuffer.wrap(buffer)));
        }

        tempAudioFile.delete();
    }

    private byte[] getAudioDataBySongId(Long songId) {
        return songRepository.findById(songId)
                .map(song -> song.getAudioFile().getFileData())
                .orElse(null);
    }
}
