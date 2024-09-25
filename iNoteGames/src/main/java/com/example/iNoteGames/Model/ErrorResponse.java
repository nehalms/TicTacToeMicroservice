package com.example.iNoteGames.Model;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ErrorResponse {

    private int StatusCode;
    private String message;
    private LocalDateTime dateTime;

    public String getMessage() {
        return message;
    }


    public int getStatusCode() {
        return StatusCode;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
