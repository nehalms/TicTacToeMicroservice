package com.example.iNoteGames.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {

    @JsonProperty("player1")
    private DbPlayer player1;

    @JsonProperty("player2")
    private DbPlayer player2;

    public DbPlayer getPlayer1() {
        return player1;
    }

    public void setPlayer1(DbPlayer player1) {
        this.player1 = player1;
    }

    public DbPlayer getPlayer2() {
        return player2;
    }

    public void setPlayer2(DbPlayer player2) {
        this.player2 = player2;
    }
}
