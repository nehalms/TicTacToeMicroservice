package com.example.iNoteGames.Model;

import lombok.Data;

@Data
public class UserStats {

    private String gameId;
    private TicToe type;
    private Player player;
}
