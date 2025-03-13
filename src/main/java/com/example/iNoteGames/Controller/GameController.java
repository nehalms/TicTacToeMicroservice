package com.example.iNoteGames.Controller;

import com.example.iNoteGames.Exception.*;
import com.example.iNoteGames.Model.*;
import com.example.iNoteGames.Service.GameService;
import com.example.iNoteGames.Service.TicTacToeBot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/game")
@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    @Autowired
    public TicTacToeBot ticTacToeBot;

    @Autowired
    public SimpMessagingTemplate simpMessagingTemplate;

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Server Status: Running");
        return new ResponseEntity<>("Hello render", HttpStatus.OK);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/start")
    public ResponseEntity<Game> createGame(@RequestBody Player player, @RequestHeader(value = "authToken", required = false) String token) throws InvalidTokenException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Game game = gameService.createGame(player);
        log.info("Game Created(Player 1): {}", game);
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/connect")
    public ResponseEntity<Game> joinGame(@RequestBody Player player, @RequestHeader(value = "authToken", required = false) String token, @RequestParam String gameId) throws GameStartedException, GameNotFoundException, GameCompletedException, DuplicatePlayerException, InvalidTokenException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Game game = gameService.joinGame(player, gameId);
        log.info("Game started(Player 2 joined): {}", game);
        String destination = "/topic/oppPlayerDetails/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/getStatus")
    public ResponseEntity<Game> getGame(@RequestBody Player player, @RequestHeader(value = "authToken", required = false) String token, @RequestParam String gameId) throws InvalidTokenException, DuplicatePlayerException, GameNotFoundException, GameCompletedException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Game game = gameService.getGame(player, gameId);
        log.info("Game Status requested: {}", game);
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/gameplay")
    public ResponseEntity<Game> playGame(@RequestHeader(value = "authToken", required = false) String token, @RequestBody GamePlay gamePlay) throws GameCompletedException, GameNotFoundException, WaitingException, InvalidTokenException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Game game = gameService.playGame(gamePlay);
        log.info("Game being played : {}", gamePlay);
        String destination = "/topic/updatedGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        gameService.updateStats(game);
        return ResponseEntity.ok(game);
    }


    @CrossOrigin(originPatterns = "*")
    @PostMapping("/reset")
    public ResponseEntity<Game> newGame(@RequestHeader(value = "authToken", required = false) String token, @RequestParam String gameId) throws GameStartedException, GameNotFoundException, InvalidTokenException, GameCompletedException, WaitingException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Game game = gameService.resetBoard(gameId);

        if((game.getTurn() == TicToe.X && game.getUserIdX().equalsIgnoreCase("Bot")) || (game.getTurn() == TicToe.O && game.getUserIdO().equalsIgnoreCase("Bot"))) {
            int[] res = ticTacToeBot.getBestMove(game.getBoard(), game.getTurn().getValue());
            GamePlay gamePlay1 = new GamePlay(game.getTurn(), res[0], res[1], game.getGameId());
            game = gameService.playGame(gamePlay1);
        }
        log.info("New Game started: {}", game);
        String destination = "/topic/resetGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/create/bot")
    public ResponseEntity<Game> vsBot(@RequestBody Player player, @RequestHeader(value = "authToken", required = false) String token) throws InvalidTokenException, GameStartedException, GameCompletedException, DuplicatePlayerException, GameNotFoundException, WaitingException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Player bot = new Player("Bot", "AiBot", 0);
        Game game1 = gameService.createGame(bot);
        Game game = gameService.joinGame(player, game1.getGameId());
        log.info("Game Created(Player 1): {}", game);

        if((game.getTurn() == TicToe.X && game.getUserIdX().equalsIgnoreCase("Bot")) || (game.getTurn() == TicToe.O && game.getUserIdO().equalsIgnoreCase("Bot"))) {
            int[] res = ticTacToeBot.getBestMove(game.getBoard(), game.getTurn().getValue());
            GamePlay gamePlay = new GamePlay(game.getTurn(), res[0], res[1], game.getGameId());
            log.info("Game being played : {}", gamePlay);
            game = gameService.playGame(gamePlay);
        }
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/gameplay/bot")
    public ResponseEntity<Game> vsBot(@RequestHeader(value = "authToken", required = false) String token, @RequestBody GamePlay gamePlay) throws GameCompletedException, GameNotFoundException, WaitingException, InvalidTokenException {
        if(token == null) {
            throw new InvalidTokenException("Authenticate using valid token");
        }
        gameService.authenticateUser(token);
        Game game = gameService.playGame(gamePlay);
        log.info("Game being played : {}", gamePlay);

        if( ((game.getTurn() == TicToe.X && game.getUserIdX().equalsIgnoreCase("Bot")) || (game.getTurn() == TicToe.O && game.getUserIdO().equalsIgnoreCase("Bot"))) && game.getStatus() == GameStatus.IN_PROGRESS) {
            int[] res = ticTacToeBot.getBestMove(game.getBoard(), game.getTurn().getValue());
            GamePlay gamePlay1 = new GamePlay(game.getTurn(), res[0], res[1], game.getGameId());
            log.info("Game being played : {}", gamePlay1);
            game = gameService.playGame(gamePlay1);
        }

        String destination = "/topic/updatedGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        gameService.updateStats(game);
        return ResponseEntity.ok(game);
    }

    @ExceptionHandler(DuplicatePlayerException.class)
    public ResponseEntity<Object> ErrorHandler(DuplicatePlayerException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GameCompletedException.class)
    public ResponseEntity<Object> ErrorHandler(GameCompletedException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<Object> ErrorHandler(GameNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GameStartedException.class)
    public ResponseEntity<Object> ErrorHandler(GameStartedException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WaitingException.class)
    public ResponseEntity<Object> ErrorHandler(WaitingException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> ErrorHandler(InvalidTokenException e) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
