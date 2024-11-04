package com.example.iNoteGames.Exception;

public class InvalidTokenException extends Throwable{
    private String message;

    public InvalidTokenException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
