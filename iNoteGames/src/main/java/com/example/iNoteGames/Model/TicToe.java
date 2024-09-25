package com.example.iNoteGames.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TicToe {
    X(1), O(2), DRAW(3);

    private Integer value;
}
