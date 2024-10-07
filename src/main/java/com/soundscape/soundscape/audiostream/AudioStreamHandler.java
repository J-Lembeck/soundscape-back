package com.soundscape.soundscape.audiostream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.mpatric.mp3agic.Mp3File;
import com.soundscape.soundscape.song.SongRepository;

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

    private void startStreaming(WebSocketSession session) throws Exception {
        InputStream audioStream = getAudioStreamBySongId(songId);

        if (audioStream == null) {
            session.sendMessage(new TextMessage("Error: Audio file not found."));
            return;
        }

        String filePath = songRepository.findById(songId).get().getAudioFile().getFilePath();
        Mp3File mp3file = new Mp3File(filePath);
        long durationInSeconds = mp3file.getLengthInSeconds();
        session.sendMessage(new TextMessage("duration:" + durationInSeconds));

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = audioStream.read(buffer)) != -1) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            session.sendMessage(new BinaryMessage(byteBuffer));
        }

        audioStream.close();
    }

    private InputStream getAudioStreamBySongId(Long songId) {
        String filePath = songRepository.findById(songId).get().getAudioFile().getFilePath();
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
