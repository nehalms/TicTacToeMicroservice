package com.example.iNoteGames.Service;

import org.springframework.stereotype.Component;

@Component
public class TicTacToeBot {

    public static final int PLAYER_X = 1;
    public static final int PLAYER_O = 2;
    public static final int EMPTY = 0;

    public int[] getBestMove(int[][] board, int currentTurn) {
        int opponent = (currentTurn == PLAYER_X) ? PLAYER_O : PLAYER_X;

        int[] winningMove = findImmediateThreat(board, currentTurn);
        if (winningMove != null) {
            return winningMove;
        }

        int[] blockMove = findImmediateThreat(board, opponent);
        if (blockMove != null) {
            return blockMove;
        }

        int[] bestMove = new int[2];
        int bestValue = (currentTurn == PLAYER_O) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    board[i][j] = currentTurn;
                    int moveValue = minimax(board, 0, currentTurn == PLAYER_O);
                    board[i][j] = EMPTY;

                    if ((currentTurn == PLAYER_O && moveValue > bestValue) ||
                            (currentTurn == PLAYER_X && moveValue < bestValue)) {
                        bestValue = moveValue;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }
        return bestMove;
    }

    private int[] findImmediateThreat(int[][] board, int player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) {
                    board[i][j] = player;
                    if (evaluate(board) == (player == PLAYER_O ? 10 : -10)) {
                        board[i][j] = EMPTY;
                        return new int[]{i, j};
                    }
                    board[i][j] = EMPTY;
                }
            }
        }
        return null;
    }

    private int minimax(int[][] board, int depth, boolean isMaximizing) {
        int score = evaluate(board);

        if (score == 10) return score;
        if (score == -10) return score;
        if (!isMovesLeft(board)) return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = PLAYER_O;
                        best = Math.max(best, minimax(board, depth + 1, false));
                        board[i][j] = EMPTY;
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = PLAYER_X;
                        best = Math.min(best, minimax(board, depth + 1, true));
                        board[i][j] = EMPTY;
                    }
                }
            }
            return best;
        }
    }

    private int evaluate(int[][] board) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                if (board[row][0] == PLAYER_O) return 10;
                else if (board[row][0] == PLAYER_X) return -10;
            }
        }
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == PLAYER_O) return 10;
                else if (board[0][col] == PLAYER_X) return -10;
            }
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == PLAYER_O) return 10;
            else if (board[0][0] == PLAYER_X) return -10;
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == PLAYER_O) return 10;
            else if (board[0][2] == PLAYER_X) return -10;
        }
        return 0;
    }

    private boolean isMovesLeft(int[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == EMPTY) return true;
            }
        }
        return false;
    }
}
