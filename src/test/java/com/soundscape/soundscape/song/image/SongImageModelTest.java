package com.soundscape.soundscape.song.image;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class SongImageModelTest {

    @Test
    void testConstructorWithImageDataAndCreationDate() {
        byte[] imageData = "test image data".getBytes();
        Date creationDate = new Date();

        SongImageModel songImage = new SongImageModel(imageData, creationDate);

        assertNotNull(songImage, "SongImageModel instance should not be null");
        assertArrayEquals(imageData, songImage.getImageData(), "Image data should match the input");
        assertEquals(creationDate, songImage.getCreationDate(), "Creation date should match the input");
    }
}
