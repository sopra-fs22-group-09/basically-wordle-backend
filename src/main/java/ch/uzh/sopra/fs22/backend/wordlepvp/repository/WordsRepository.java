package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.*;

@Component
public class WordsRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final String allWords = "allWords";

    public WordsRepository(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String[] getWordsByTopic(String topic, int count) {
        if (count < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please request a valid number of words.");
        String sanitizedTopic = topic.replaceAll("[^A-Za-z]", ""); //Sanitize all non-alphabetic characters to prevent API abuse
        if (sanitizedTopic.equals("")) sanitizedTopic = allWords;

        //Check whether there are enough words in the repository:
        //If not fetch from api and check again. if there are still not enough words throw error.
        if (count > redisTemplate.opsForHash().size(sanitizedTopic)) {
            cacheWordsFromAPI(sanitizedTopic);
            if (count > redisTemplate.opsForHash().size(sanitizedTopic))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many words for the were requested.");
        }

        //Convert result to object array, then copy the content to a new string array and return it.
        return Arrays.copyOf(Objects.requireNonNull(redisTemplate.opsForHash().randomEntries(sanitizedTopic, count)).values().toArray(), count, String[].class);
    }

    public String[] getWordsByTopics(String[] topics, int count) {
        if (topics.length < 1) return getRandomWords(count);
        if (topics.length == 1) return getWordsByTopic(topics[0], count);
        String[][] tmp = new String[topics.length][];
        for (int i = 0; i < topics.length; ++i) tmp[i] = getWordsByTopic(topics[i], count);
        String[] words = new String[count];
        Random rnd = new Random();
        for (int i = 0; i < count; ++i) {
            int y = rnd.nextInt(topics.length);
            int x = rnd.nextInt(count);
            if (tmp[y][x] != null) { //Prevent selecting more than once the same word
                words[i] = tmp[y][x];
                tmp[y][x] = null;
            } else --i;
        }
        return words;
    }

    public String[] getRandomWords(int count) {
        return getWordsByTopic(allWords, count);
    }

    private void cacheWordsFromAPI(String topic) {
        BufferedReader input = null;
        try {
            URL url = new URL("https://api.datamuse.com/words?sp=?????&max=1000" + (topic.equals(allWords) ? "" : "&topics=" + topic));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if(conn.getResponseCode() == 200) {
                input = new BufferedReader(new InputStreamReader(url.openStream()));
                HashMap<String, String> tmp = new HashMap<>();
                String inputLine;
                while((inputLine = input.readLine()) != null) {
                    for (Object j : (JSONArray) new JSONParser().parse(inputLine)) {
                        String word = ((JSONObject) j).get("word").toString();
                        // Don't establish a connection to redis every time
                        // Instead collect all words and put them all together to redis
                        tmp.put(word, word);
                    }
                }
                redisTemplate.opsForHash().putAll(topic, tmp);
                redisTemplate.expire(topic, Duration.ofDays(1));
            } else throw new ProtocolException();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while fetching data from the external API. Please try again in a few minutes.");
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while processing the fetched data from the external API. Please try again in a few minutes.");
        }
        finally {
            try {
                if (input != null) input.close();
            } catch (IOException ignore) {}
        }
    }
}