package com.soundscape.soundscape.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
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
    void testConstructorWithPresigner() {
        S3Presigner mockPresigner = S3Presigner.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(() -> AwsBasicCredentials.create("testAccessKey", "testSecretKey"))
                .build();

        S3Service s3Service = new S3Service(mockPresigner);

        assertNotNull(s3Service, "S3Service instance should not be null when created with presigner");
    }

    @Test
    void testConstructorWithCredentials() {
        String accessKey = "testAccessKey";
        String secretKey = "testSecretKey";
        Region region = Region.US_EAST_1;

        S3Service s3Service = new S3Service(accessKey, secretKey, region);

        assertNotNull(s3Service, "S3Service instance should not be null when created with credentials");
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
