package com.soundscape.soundscape.audiostream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.mpatric.mp3agic.Mp3File;

public class AudioStreamHandler extends BinaryWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        InputStream audioStream = getClass().getResourceAsStream("/com/soundscape/soundscape/audiostream/sample.mp3");

        File tempFile = File.createTempFile("sample", ".mp3");
        tempFile.deleteOnExit();

        int bytesRead;
        byte[] buffer = new byte[1024];

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        Mp3File mp3file = new Mp3File(tempFile.getAbsolutePath());
        long durationInSeconds = mp3file.getLengthInSeconds();

        session.sendMessage(new TextMessage("duration:" + durationInSeconds));

        audioStream = getClass().getResourceAsStream("/com/soundscape/soundscape/audiostream/sample.mp3");

        while ((bytesRead = audioStream.read(buffer)) != -1) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            session.sendMessage(new BinaryMessage(byteBuffer));
        }

        audioStream.close();
    }
}