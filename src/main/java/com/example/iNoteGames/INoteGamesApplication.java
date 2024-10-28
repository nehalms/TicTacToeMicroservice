package com.example.iNoteGames;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class INoteGamesApplication {

	public static void main(String[] args) {
		SpringApplication.run(INoteGamesApplication.class, args);
	}

}
