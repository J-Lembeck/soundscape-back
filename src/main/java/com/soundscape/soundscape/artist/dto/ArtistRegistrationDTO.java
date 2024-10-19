package com.soundscape.soundscape.artist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistRegistrationDTO {

    private String name;
    private String email;
    private String password;

}
