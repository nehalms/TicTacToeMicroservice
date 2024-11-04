package com.example.iNoteGames.Controller;

import com.example.iNoteGames.Exception.*;
import com.example.iNoteGames.Model.*;
import com.example.iNoteGames.Service.GameService;
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
    public SimpMessagingTemplate simpMessagingTemplate;

    @CrossOrigin(originPatterns = "*")
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello render", HttpStatus.OK);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/start")
    public ResponseEntity<Game> createGame(@RequestBody Player player, @RequestHeader(value = "auth-token", required = false) String token) throws InvalidTokenException {
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
    public ResponseEntity<Game> joinGame(@RequestBody Player player, @RequestHeader(value = "auth-token", required = false) String token, @RequestParam String gameId) throws GameStartedException, GameNotFoundException, GameCompletedException, DuplicatePlayerException, InvalidTokenException {
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
    @PostMapping("/gameplay")
    public ResponseEntity<Game> playGame(@RequestBody GamePlay gamePlay) throws GameCompletedException, GameNotFoundException, WaitingException {
        Game game = gameService.playGame(gamePlay);
        log.info("Game being played : {}", gamePlay);
        String destination = "/topic/updatedGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        gameService.updateStats(game);
        return ResponseEntity.ok(game);
    }


    @CrossOrigin(originPatterns = "*")
    @PostMapping("/reset")
    public ResponseEntity<Game> newGame(@RequestParam String gameId) throws GameStartedException, GameNotFoundException {
        Game game = gameService.resetBoard(gameId);
        log.info("New Game started: {}", game);
        String destination = "/topic/resetGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
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
