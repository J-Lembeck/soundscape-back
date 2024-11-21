package com.soundscape.soundscape.artist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArtistModelTest {

    private ArtistModel artist;
    private ArtistModel follower;

    @BeforeEach
    void setUp() {
        artist = new ArtistModel();
        artist.setId(1L);
        artist.setName("Main Artist");

        follower = new ArtistModel();
        follower.setId(2L);
        follower.setName("Follower Artist");
    }

    @Test
    void testEquals_SameInstance() {
        assertTrue(artist.equals(artist), "Artist should be equal to itself");
    }

    @Test
    void testEquals_NullObject() {
        assertFalse(artist.equals(null), "Artist should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        assertFalse(artist.equals("Not an Artist"), "Artist should not be equal to an object of a different class");
    }

    @Test
    void testEquals_DifferentIds() {
        ArtistModel anotherArtist = new ArtistModel();
        anotherArtist.setId(3L);

        assertFalse(artist.equals(anotherArtist), "Artists with different IDs should not be equal");
    }

    @Test
    void testEquals_SameIds() {
        ArtistModel anotherArtist = new ArtistModel();
        anotherArtist.setId(1L);

        assertTrue(artist.equals(anotherArtist), "Artists with the same ID should be equal");
    }

    @Test
    void testHashCode_SameIds() {
        ArtistModel anotherArtist = new ArtistModel();
        anotherArtist.setId(1L);

        assertEquals(artist.hashCode(), anotherArtist.hashCode(), "Artists with the same ID should have the same hash code");
    }

    @Test
    void testHashCode_DifferentIds() {
        ArtistModel anotherArtist = new ArtistModel();
        anotherArtist.setId(3L);

        assertNotEquals(artist.hashCode(), anotherArtist.hashCode(), "Artists with different IDs should have different hash codes");
    }

    @Test
    void testRemoveFollower_Success() {
        artist.getFollowers().add(follower);
        follower.getFollowers().add(artist);

        artist.removeFollower(follower);

        assertFalse(artist.getFollowers().contains(follower), "Follower should be removed from the artist's followers");
        assertFalse(follower.getFollowers().contains(artist), "Artist should be removed from the follower's followers");
    }

    @Test
    void testRemoveFollower_WhenNotInFollowers() {
        artist.removeFollower(follower);

        assertFalse(artist.getFollowers().contains(follower), "Follower should not be in the artist's followers");
        assertFalse(follower.getFollowers().contains(artist), "Artist should not be in the follower's followers");
    }

    @Test
    void testRemoveFollower_NoCircularReference() {
        artist.getFollowers().add(follower);
        follower.getFollowers().add(artist);

        artist.removeFollower(follower);

        assertFalse(artist.getFollowers().contains(follower), "Follower should no longer be in the artist's followers");
        assertFalse(follower.getFollowers().contains(artist), "Artist should no longer be in the follower's followers");
    }
}