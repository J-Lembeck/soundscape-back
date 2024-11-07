package com.soundscape.soundscape.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class S3ServiceTest {

    private S3Service s3Service;

    @Mock
    private S3Presigner presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        URL mockUrl = mock(URL.class);
        when(mockUrl.toString()).thenReturn("https://mockurl.com/presigned-url");
        when(presignedGetObjectRequest.url()).thenReturn(mockUrl);

        when(presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedGetObjectRequest);

        s3Service = new S3Service(presigner);
    }

    @Test
    void generatePresignedUrl_shouldReturnValidUrl() {
        String bucketName = "test-bucket";
        String key = "test-object";

        String presignedUrl = s3Service.generatePresignedUrl(bucketName, key);

        verify(presigner).presignGetObject(any(GetObjectPresignRequest.class));

        assertEquals("https://mockurl.com/presigned-url", presignedUrl);
    }
}
