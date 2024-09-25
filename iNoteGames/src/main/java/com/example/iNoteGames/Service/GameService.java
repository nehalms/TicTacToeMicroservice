package com.example.iNoteGames.Service;

import com.example.iNoteGames.Exception.*;
import com.example.iNoteGames.Model.*;
import com.javamaster.storage.GameStorage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@NoArgsConstructor
public class GameService {

    public Game createGame(Player player) {
        Game game = new Game();
        game.setGameId(UUID.randomUUID().toString());
        game.setPlayer1(player);
        game.setStatus(GameStatus.NEW);
        game.setBoard(new int[3][3]);
        game.setWinnerIdxs(new int[3]);
        game.setTurn(TicToe.X);
        GameStorage.getInstance().setGame(game);
        return game;
    }

    public Game joinGame(Player player, String gameId) throws GameNotFoundException, GameStartedException, GameCompletedException, DuplicatePlayerException {
        if(!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new GameNotFoundException("No game found with the given ID");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);
        if(game.getStatus() == GameStatus.IN_PROGRESS) {
            throw new GameStartedException("Game you are trying to join is already started");
        }
        if(game.getStatus() == GameStatus.FINISHED) {
            throw new GameCompletedException("Game is already Completed");
        }
        if(game.getPlayer1().equals(player)) {
            throw new DuplicatePlayerException("Already in the room");
        }
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setPlayer2(player);
        GameStorage.getInstance().setGame(game);

        return game;
    }

    public Game playGame(GamePlay gamePlay) throws GameNotFoundException, GameCompletedException, WaitingException {
        if(!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId())) {
            throw new GameNotFoundException("No Game found with the given Id");
        }
        Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());
        if(game.getStatus() == GameStatus.NEW) {
            throw new WaitingException("Waiting for the other player to join");
        }
        if(game.getStatus() == GameStatus.FINISHED) {
            throw new GameCompletedException("Game is already Completed");
        }

        int board[][] = game.getBoard();
        board[gamePlay.getCoordinateX()][gamePlay.getCoordinateY()] = gamePlay.getType().getValue();

        boolean winX = checkWinner(game, board, TicToe.X);
        boolean winO = checkWinner(game, board, TicToe.O);

        if(winX) {
            game.setWinner(TicToe.X);
        } else if(winO) {
            game.setWinner(TicToe.O);
        }

        boolean completed = true;
        for(int i=0; i<board.length; i++) {
            for(int j=0; j<board[0].length; j++) {
                if(board[i][j] == 0) {
                    completed = false;
                }
            }
        }
        if(completed && (!winX && !winO)) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(TicToe.DRAW);
        }
        if(winX || winO) {
            game.setStatus(GameStatus.FINISHED);
        }
        game.setTurn(game.getTurn() == TicToe.X ? TicToe.O : TicToe.X);
        GameStorage.getInstance().setGame(game);

        return game;
    }

    public Game resetBoard(String gameId) throws GameStartedException, GameNotFoundException {
        if(!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new GameNotFoundException("No Game found with the given Id");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);
        game.setBoard(new int[3][3]);
        game.setTurn(TicToe.X);
        game.setWinner(null);
        game.setWinnerIdxs(new int[3]);
        game.setStatus(GameStatus.IN_PROGRESS);
        GameStorage.getInstance().setGame(game);

        return game;
    }

    public boolean checkWinner(Game game, int board[][], TicToe ticToe) {
        int oneDBoard[] = new int[9];
        int idx = 0;
        for(int i=0; i<board.length; i++) {
            for(int j=0; j<board[0].length; j++) {
                oneDBoard[idx++] = board[i][j];
            }
        }

        int[][] winCombinations = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for(int i=0; i<winCombinations.length; i++) {
            if(oneDBoard[winCombinations[i][0]] == ticToe.getValue()
                    && oneDBoard[winCombinations[i][1]] == ticToe.getValue()
                    && oneDBoard[winCombinations[i][2]] == ticToe.getValue()) {
                game.setWinnerIdxs(new int[]{winCombinations[i][0], winCombinations[i][1], winCombinations[i][2]});
                return true;
            }
        }
        return false;
    }
}
