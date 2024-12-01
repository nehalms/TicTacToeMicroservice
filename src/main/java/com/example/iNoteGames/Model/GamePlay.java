package com.example.iNoteGames.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GamePlay {
    private TicToe type;
    private Integer coordinateX;
    private Integer coordinateY;
    private String gameId;
}
