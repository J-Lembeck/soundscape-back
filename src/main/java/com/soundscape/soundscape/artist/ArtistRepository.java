package com.soundscape.soundscape.artist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<ArtistModel, Long> {

	Optional<ArtistModel> findByName(String username);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

}
