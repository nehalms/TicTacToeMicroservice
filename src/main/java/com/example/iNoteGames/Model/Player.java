package com.example.iNoteGames.Model;

import lombok.Data;

import java.util.Objects;

@Data
public class Player {

    private String name;
    private int gamesPlayed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return gamesPlayed == player.gamesPlayed && Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, gamesPlayed);
    }

}
