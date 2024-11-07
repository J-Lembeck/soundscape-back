package com.soundscape.soundscape.audiostream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.mpatric.mp3agic.InvalidDataException;
import com.soundscape.soundscape.audiofile.AudioFileModel;
import com.soundscape.soundscape.s3.S3Service;
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

        audioStreamHandler = Mockito.spy(new AudioStreamHandler(songRepository));

        when(webSocketSession.isOpen()).thenReturn(true);

        String s3Key = "audio/test-file.mp3";

        AudioFileModel audioFile = new AudioFileModel();
        audioFile.setFilePath(s3Key);

        mockSong = new SongModel();
        mockSong.setAudioFile(audioFile);

        when(songRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(mockSong));

        mockAudioData = Files.readAllBytes(Paths.get("src/test/resources/test-file.mp3"));
        InputStream inputStream = new ByteArrayInputStream(mockAudioData);

        doReturn(inputStream).when(audioStreamHandler).downloadAudioFromS3(s3Key);
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

    @Test
    public void testHandleTextMessage_SuccessfulStreaming() throws Exception {
        String s3Key = "audio/test-file.mp3";
        String bucketName = "soundscape-files";
        String presignedUrl = "http://dummy-presigned-url";

        when(songRepository.findSongWithAudioFileById(any(Long.class)))
            .thenReturn(java.util.Optional.of(mockSong));

        S3Service s3ServiceMock = mock(S3Service.class);
        when(s3ServiceMock.generatePresignedUrl(bucketName, s3Key)).thenReturn(presignedUrl);

        doReturn(s3ServiceMock).when(audioStreamHandler).createS3Service();

        InputStream inputStreamMock = new ByteArrayInputStream(mockAudioData);
        doReturn(inputStreamMock).when(audioStreamHandler).openStream(presignedUrl);

        TextMessage message = new TextMessage("songId:1");
        audioStreamHandler.handleTextMessage(webSocketSession, message);

        verify(webSocketSession, times(1)).sendMessage(argThat(argument ->
            argument instanceof TextMessage && ((TextMessage) argument).getPayload().startsWith("duration:")
        ));

        verify(webSocketSession, atLeastOnce()).sendMessage(any(BinaryMessage.class));
    }

    @Test
    public void testDownloadAudioFromS3() throws Exception {
        String audioFilePath = "http://dummy-s3-url";
        byte[] mockData = "Test audio data".getBytes();
        InputStream mockInputStream = new ByteArrayInputStream(mockData);

        AudioStreamHandler audioStreamHandlerSpy = spy(new AudioStreamHandler(null));

        URL mockUrl = mock(URL.class);
        doReturn(mockUrl).when(audioStreamHandlerSpy).createUrl(audioFilePath);
        when(mockUrl.openStream()).thenReturn(mockInputStream);

        InputStream resultInputStream = audioStreamHandlerSpy.downloadAudioFromS3(audioFilePath);

        assertNotNull(resultInputStream);
        byte[] resultData = resultInputStream.readAllBytes();
        assertArrayEquals(mockData, resultData);

        verify(audioStreamHandlerSpy, times(1)).createUrl(audioFilePath);
        verify(mockUrl, times(1)).openStream();
    }

    @Test
    public void testStartStreaming_EmptyAudioData() throws Exception {
        String s3Key = "audio/test-file.mp3";
        String bucketName = "soundscape-files";
        String presignedUrl = "http://dummy-presigned-url";

        when(songRepository.findSongWithAudioFileById(any(Long.class)))
            .thenReturn(Optional.of(mockSong));

        S3Service s3ServiceMock = mock(S3Service.class);
        when(s3ServiceMock.generatePresignedUrl(bucketName, s3Key)).thenReturn(presignedUrl);
        doReturn(s3ServiceMock).when(audioStreamHandler).createS3Service();

        InputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);
        doReturn(emptyInputStream).when(audioStreamHandler).openStream(presignedUrl);

        TextMessage message = new TextMessage("songId:1");
        audioStreamHandler.handleTextMessage(webSocketSession, message);

        verify(webSocketSession, times(1)).sendMessage(argThat(argument ->
            argument instanceof TextMessage && ((TextMessage) argument).getPayload().equals("Error: Downloaded audio file is empty.")
        ));

        verify(webSocketSession, never()).sendMessage(any(BinaryMessage.class));
    }

    @Test
    public void testStartStreaming_InvalidMp3Data() throws Exception {
        String s3Key = "audio/test-file.mp3";
        String bucketName = "soundscape-files";
        String presignedUrl = "http://dummy-presigned-url";

        when(songRepository.findSongWithAudioFileById(any(Long.class)))
            .thenReturn(Optional.of(mockSong));

        S3Service s3ServiceMock = mock(S3Service.class);
        when(s3ServiceMock.generatePresignedUrl(bucketName, s3Key)).thenReturn(presignedUrl);
        doReturn(s3ServiceMock).when(audioStreamHandler).createS3Service();

        byte[] invalidMp3Data = "invalid mp3 data".getBytes();
        InputStream inputStreamMock = new ByteArrayInputStream(invalidMp3Data);
        doReturn(inputStreamMock).when(audioStreamHandler).openStream(presignedUrl);

        doThrow(new InvalidDataException("Invalid MP3 data")).when(audioStreamHandler).createMp3File(any(File.class));

        TextMessage message = new TextMessage("songId:1");
        audioStreamHandler.handleTextMessage(webSocketSession, message);

        verify(webSocketSession, times(1)).sendMessage(argThat(argument ->
            argument instanceof TextMessage && ((TextMessage) argument).getPayload().equals("Error: Invalid MP3 data.")
        ));

        verify(webSocketSession, never()).sendMessage(any(BinaryMessage.class));
    }

}
