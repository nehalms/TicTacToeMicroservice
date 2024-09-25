package com.example.iNoteGames.Exception;

public class DuplicatePlayerException extends Throwable{

    private String message;

    public DuplicatePlayerException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
