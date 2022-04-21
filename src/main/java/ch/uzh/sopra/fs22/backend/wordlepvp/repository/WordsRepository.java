package ch.uzh.sopra.fs22.backend.wordlepvp.repository;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.Scanner;

@Component
public class WordsRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String allWords = "allWords";

    public WordsRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String[] getWordsByTopic(String topic, int count) {
        if (count < 1) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Please request a valid number of words."); // TODO should this error be handled differently?
        if (count > redisTemplate.opsForHash().size(topic)) {
            cacheWordsFromAPI(topic.equals(allWords) ? "" : topic);
            if (count > redisTemplate.opsForHash().size(topic))
                throw new ResponseStatusException(HttpStatus.PARTIAL_CONTENT, "Too many words for the were requested."); // TODO should this error be handled differently?
        }

        String[] words = new String[count];
        boolean skip = false;
        for (int i = 0; i < words.length; ++i) {
            String word = redisTemplate.opsForHash().randomEntry(topic).getValue().toString();

            for (int j = 0; j < i; ++j) {
                skip = words[j].equals(word);
                if (skip) break;
            }

            if (skip) --i;
            else words[i] = word;
        }
        return words;
    }

    public String[] getRandomWords(int count) {
        return getWordsByTopic(allWords, count);
    }

    private void cacheWordsFromAPI(String topic) {
        try {
            URL url = new URL("https://api.datamuse.com/words?sp=?????&max=1000&topics=" + topic);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if(conn.getResponseCode() == 200) {
                Scanner scan = new Scanner(url.openStream());
                while(scan.hasNext()) {
                    JSONArray js = (JSONArray) new JSONParser().parse(scan.nextLine());

                    for (int i = 0; i < js.size(); ++i) {
                        String word = ((JSONObject) js.get(i)).get("word").toString();
                        redisTemplate.opsForHash().put(topic, word, word);
                        // TODO if one topic is chosen often allWords will always contain (at least) words of this topic
                        //  but it should contain words of all topics
                        redisTemplate.opsForHash().put(allWords, word, word);
                    }
                }
                redisTemplate.expire(topic, Duration.ofDays(1));
                if (redisTemplate.getExpire(allWords) != null && redisTemplate.getExpire(allWords) <= 0)
                    redisTemplate.expire(allWords, Duration.ofDays(1));


            } else throw new ProtocolException();
        } catch (ProtocolException | MalformedURLException e) {
            // TODO should this error be handled differently?
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong while fetching data from the external API. Please try again in a few minutes.");
        } catch (IOException | ParseException e) {
            // TODO should this error be handled differently?
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "Something went wrong while processing the fetched data from the external API. Please try again in a few minutes.");
        }
    }
}