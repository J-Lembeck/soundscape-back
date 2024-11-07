package com.soundscape.soundscape.websocket.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.soundscape.soundscape.audiostream.AudioStreamHandler;

public class WebSocketConfigTest {

    @Mock
    private AudioStreamHandler audioStreamHandler;

    @Mock
    private WebSocketHandlerRegistry registry;

    @Mock
    private WebSocketHandlerRegistration registration;

    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webSocketConfig = new WebSocketConfig(audioStreamHandler);
    }

    @Test
    void registerWebSocketHandlers_shouldRegisterAudioStreamHandlerWithCorrectPathAndAllowedOrigins() {
        when(registry.addHandler(audioStreamHandler, "/audio-stream")).thenReturn(registration);

        webSocketConfig.registerWebSocketHandlers(registry);

        verify(registry).addHandler(audioStreamHandler, "/audio-stream");

        verify(registration).setAllowedOrigins("*");
    }
}
