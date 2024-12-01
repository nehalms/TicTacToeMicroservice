package com.example.iNoteGames.Service;

import com.example.iNoteGames.Exception.*;
import com.example.iNoteGames.Model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javamaster.storage.GameStorage;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

@Service
@NoArgsConstructor
public class GameService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment environment;

    public Game createGame(Player player) {
        Game game = new Game();
        game.setGameId(UUID.randomUUID().toString());
        game.setPlayer1(player);
        game.setUserIdX(player.getUserId());
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
        if(game.getPlayer1().getUserId().equals(player.getUserId())) {
            throw new DuplicatePlayerException("Already in the room");
        }
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setPlayer2(player);
        game.setUserIdO(player.getUserId());
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
        for (int[] ints : board) {
            for (int j = 0; j < board[0].length; j++) {
                if (ints[j] == 0) {
                    completed = false;
                    break;
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

    @Async
    public void updateStats(Game game) {
        if(game.getUserIdO().equalsIgnoreCase("Bot") || game.getUserIdX().equalsIgnoreCase("Bot")) {
            return;
        }
        if(game.getStatus() == GameStatus.FINISHED) {
            int player1Stat = game.getWinner() == TicToe.DRAW || game.getWinner() == TicToe.O ? 0 : 1;
            int player2Stat = game.getWinner() == TicToe.DRAW || game.getWinner() == TicToe.X ? 0 : 1;
            String apiKey = environment.getProperty("APIKEY");
            String url = "https://inotebookapi-nehals-projects-e27269ee.vercel.app/api/game/tttsave/" + game.getUserIdX() + "/" + player1Stat + "/" + game.getUserIdO() + "/" + player2Stat + "/" + apiKey;
            System.out.println("Url: " + url);
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Response: " + response);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ApiResponse apiResponse = objectMapper.readValue(response, ApiResponse.class);
                Player player1 = game.getPlayer1();
                Player player2 = game.getPlayer2();
                if(player1.getUserId().equalsIgnoreCase(apiResponse.getData().getPlayer1().getUserId())) {
                    player1.setGamesPlayed(apiResponse.getData().getPlayer1().getTttStats().getPlayed());
                    player2.setGamesPlayed(apiResponse.getData().getPlayer2().getTttStats().getPlayed());
                } else if(player1.getUserId().equalsIgnoreCase(apiResponse.getData().getPlayer2().getUserId())) {
                    player1.setGamesPlayed(apiResponse.getData().getPlayer2().getTttStats().getPlayed());
                    player2.setGamesPlayed(apiResponse.getData().getPlayer1().getTttStats().getPlayed());
                }
                game.setPlayer1(player1);
                game.setPlayer2(player2);
            } catch (Exception e) {
                System.err.println("Error parsing response: " + e.getMessage());
                e.printStackTrace();
            }
            if(GameStorage.getInstance().getGames().containsKey(game.getGameId())) {
                GameStorage.getInstance().setGame(game);
            }
        }
    }

    public Game resetBoard(String gameId) throws GameNotFoundException, GameStartedException {
        if(!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new GameNotFoundException("No Game found with the given Id");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);
        if(game.getStatus() == GameStatus.IN_PROGRESS) {
            throw new GameStartedException("Game In progress");
        }
        String userIdX = game.getUserIdX();
        game.setUserIdX(game.getUserIdO());
        game.setUserIdO(userIdX);
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
        for (int[] ints : board) {
            for (int j = 0; j < board[0].length; j++) {
                oneDBoard[idx++] = ints[j];
            }
        }

        int[][] winCombinations = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};
        for (int[] winCombination : winCombinations) {
            if (oneDBoard[winCombination[0]] == ticToe.getValue()
                    && oneDBoard[winCombination[1]] == ticToe.getValue()
                    && oneDBoard[winCombination[2]] == ticToe.getValue()) {
                game.setWinnerIdxs(new int[]{winCombination[0], winCombination[1], winCombination[2]});
                return true;
            }
        }
        return false;
    }

    public void authenticateUser(String token) throws InvalidTokenException {
//        String url = "https://inotebookapi-nehals-projects-e27269ee.vercel.app/api/game/authenticateUser/" + token;
        String url = "http://localhost:8900/api/game/authenticateUser/" + token;
        String response = restTemplate.getForObject(url, String.class);
        System.out.println("Authenticated User : " + response);
        if(response.equalsIgnoreCase("false")){
            throw new InvalidTokenException("Authenticate using valid token");
        }
    }
}
