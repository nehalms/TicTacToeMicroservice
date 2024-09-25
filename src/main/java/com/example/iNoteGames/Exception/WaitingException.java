package com.example.iNoteGames.Exception;

public class WaitingException extends Throwable{

    private String message;

    public WaitingException(String message) {
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
