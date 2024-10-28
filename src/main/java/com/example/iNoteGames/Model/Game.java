package com.example.iNoteGames.Model;

import lombok.Data;

@Data
public class Game {
    private String gameId;
    private String userIdX;
    private String userIdO;
    private Player player1;
    private Player player2;
    private GameStatus status;
    private TicToe turn;
    private int[][] board;
    private TicToe winner;
    private int[] winnerIdxs;
}
