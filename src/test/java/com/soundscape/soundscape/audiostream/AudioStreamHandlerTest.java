package com.soundscape.soundscape.audiostream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.song.SongModel;
import com.soundscape.soundscape.song.SongRepository;

public class AudioStreamHandlerTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private WebSocketSession webSocketSession;

    private AudioStreamHandler audioStreamHandler;

    private SongModel mockSong;
    private byte[] mockAudioData;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        audioStreamHandler = new AudioStreamHandler(songRepository);

        when(webSocketSession.isOpen()).thenReturn(true);

        mockAudioData = Files.readAllBytes(Paths.get("src/test/resources/test-file.mp3"));
        
        AudioFileModel audioFile = new AudioFileModel();
        audioFile.setFileData(mockAudioData);

        mockSong = new SongModel();
        mockSong.setAudioFile(audioFile);

        when(songRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(mockSong));
    }

    @Test
    public void testHandleTextMessage_ValidSongId() throws Exception {
        TextMessage message = new TextMessage("songId:1");

        audioStreamHandler.handleTextMessage(webSocketSession, message);

        verify(webSocketSession, times(1)).sendMessage(argThat(argument -> 
            argument instanceof TextMessage && ((TextMessage) argument).getPayload().startsWith("duration:")
        ));

        verify(webSocketSession, atLeastOnce()).sendMessage(any(BinaryMessage.class));
    }

    @Test
    public void testHandleTextMessage_InvalidSongId() throws Exception {
        when(songRepository.findById(any(Long.class))).thenReturn(java.util.Optional.empty());

        TextMessage message = new TextMessage("songId:999");

        audioStreamHandler.handleTextMessage(webSocketSession, message);

        verify(webSocketSession, times(1)).sendMessage(argThat(argument -> 
            argument instanceof TextMessage && ((TextMessage) argument).getPayload().equals("Error: Audio file not found.")
        ));
    }

    @Test
    public void testHandleTextMessage_StreamingChunks() throws Exception {
        TextMessage message = new TextMessage("songId:1");

        audioStreamHandler.handleTextMessage(webSocketSession, message);

        int expectedChunks = (int) Math.ceil((double) mockAudioData.length / 1024);

        verify(webSocketSession, times(expectedChunks)).sendMessage(any(BinaryMessage.class));
    }

    @Test
    public void testHandleTextMessage_CloseExistingSession() throws Exception {
        TextMessage firstMessage = new TextMessage("songId:1");
        audioStreamHandler.handleTextMessage(webSocketSession, firstMessage);

        WebSocketSession anotherWebSocketSession = org.mockito.Mockito.mock(WebSocketSession.class);
        when(anotherWebSocketSession.isOpen()).thenReturn(true);

        TextMessage secondMessage = new TextMessage("songId:2");
        audioStreamHandler.handleTextMessage(anotherWebSocketSession, secondMessage);

        verify(webSocketSession, times(1)).close();
    }

    @Test
    public void testHandleTextMessage_StreamingException() throws Exception {
        doThrow(new RuntimeException("Simulated exception")).when(songRepository).findById(any(Long.class));

        TextMessage message = new TextMessage("songId:1");

        audioStreamHandler.handleTextMessage(webSocketSession, message);

        verify(webSocketSession, never()).sendMessage(any(BinaryMessage.class));
    }
}
