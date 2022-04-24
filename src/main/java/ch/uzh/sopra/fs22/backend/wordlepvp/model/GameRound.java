package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import ch.uzh.sopra.fs22.backend.wordlepvp.repository.WordsRepository;
import lombok.Builder;
import lombok.Data;

import java.util.Random;

@Data
public class GameRound {

    private WordsRepository wordRepository;

    public GameRound() {
        Random r = new Random();
        wordsArray = wordRepository.getRandomWords(250);
        targetWord = wordsArray[r.nextInt(wordsArray.length)];
    }

    private String[] wordsArray;
    private String targetWord;

    private String[] words;
    private LetterState[][] letterStates;

    private long start;
    private long finish;

}
