package com.example.iNoteGames.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class Player {

    private String userId;
    private String name;
    private int gamesPlayed;

    @Override
    public int hashCode() {
        return Objects.hash(name, gamesPlayed);
    }

}
