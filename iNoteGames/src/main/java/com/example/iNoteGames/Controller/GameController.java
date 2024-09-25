package com.example.iNoteGames.Controller;

import com.example.iNoteGames.Exception.*;
import com.example.iNoteGames.Model.*;
import com.example.iNoteGames.Service.GameService;
import com.javamaster.storage.GameStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
    @PostMapping("/start")
    public ResponseEntity<Game> createGame(@RequestBody Player player) {
        Game game = gameService.createGame(player);
        log.info("Game Created(Player 1): { " + game.getGameId() +  " }");
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/connect")
    public ResponseEntity<Game> joinGame(@RequestBody Player player, @RequestParam String gameId) throws GameStartedException, GameNotFoundException, GameCompletedException, DuplicatePlayerException {
        Game game = gameService.joinGame(player, gameId);
        log.info("Game started(Player 2 joined): { " + game.getGameId() + " }");
        String destination = "/topic/oppPlayerDetails/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        return ResponseEntity.ok(game);
    }

    @CrossOrigin(originPatterns = "*")
    @PostMapping("/gameplay")
    public ResponseEntity<Game> playGame(@RequestBody GamePlay gamePlay) throws GameCompletedException, GameNotFoundException, WaitingException {
        log.info("Game being played : { " + gamePlay.getGameId() + " }");
        Game game = gameService.playGame(gamePlay);
        String destination = "/topic/updatedGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        return ResponseEntity.ok(game);
    }


    @CrossOrigin(originPatterns = "*")
    @PostMapping("/reset")
    public ResponseEntity<Game> newGame(@RequestParam String gameId) throws GameStartedException, GameNotFoundException {
        Game game = gameService.resetBoard(gameId);
        log.info("New Game started: { " + game.getGameId() + " }");
        String destination = "/topic/resetGame/" + game.getGameId();
        simpMessagingTemplate.convertAndSend(destination, game);
        return ResponseEntity.ok(game);
    }

    @MessageMapping("/sendMessage/stats")
    public void sendMessage(@Payload UserStats userStats) {
        if(!GameStorage.getInstance().getGames().containsKey(userStats.getGameId())) {
            return;
        }
        log.info("UserStats updated : " + userStats);
        Game game = GameStorage.getInstance().getGames().get(userStats.getGameId());
        if(userStats.getType() == TicToe.X) {
            game.setPlayer1(userStats.getPlayer());
        } else if(userStats.getType() == TicToe.O) {
            game.setPlayer2(userStats.getPlayer());
        }
        log.info("Updated game : " + game);
        GameStorage.getInstance().setGame(game);
        return;
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
}
