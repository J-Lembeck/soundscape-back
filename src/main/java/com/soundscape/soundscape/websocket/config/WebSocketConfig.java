package com.soundscape.soundscape.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.soundscape.soundscape.audiostream.AudioStreamHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AudioStreamHandler audioStreamHandler;

    public WebSocketConfig(AudioStreamHandler audioStreamHandler) {
        this.audioStreamHandler = audioStreamHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(audioStreamHandler, "/audio-stream").setAllowedOrigins("*");
    }
}
