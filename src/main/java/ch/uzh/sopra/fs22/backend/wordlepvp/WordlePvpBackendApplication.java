package ch.uzh.sopra.fs22.backend.wordlepvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
public class WordlePvpBackendApplication {

	public static void main(String[] args) {
		// TODO: Initialize lobbies?
		SpringApplication.run(WordlePvpBackendApplication.class, args);
	}

}
