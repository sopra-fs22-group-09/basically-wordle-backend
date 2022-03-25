package ch.uzh.sopra.fs22.backend.wordlepvp.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.support.RedisRepositoryFactoryBean;

@SuppressWarnings("CommentedOutCode")
@Configuration
@ConfigurationProperties("spring.redis")
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    @Getter
    @Setter
    private String url;

    @Bean
    public RedisRepositoryFactoryBean redisRepositoryFactoryBean() {
        return null;
    }

    // More on Redis: https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#redis:reactive:pubsub

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder -> {
            if (clientConfigurationBuilder.build().isUseSsl()) {
                clientConfigurationBuilder.useSsl().disablePeerVerification();
            }
        };
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        if (url != null)
            return new LettuceConnectionFactory(
                    LettuceConnectionFactory.createRedisConfiguration(this.url));
        return new LettuceConnectionFactory();
    }

    // This works but will cause Redis to serialize using JdkSerializationRedisSerializer which ... is ugly.
    // The potential solution can be found below
    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // TODO: Add some specific configuration here. Key serializers, etc.
        return template;
    }

    // TODO: Whoever can figure this out gets a beer (Bean "Class" can not be resolved)
//    @Bean("redisTemplate")
//    @Autowired
//    public <T, V> RedisTemplate<T, V> redisTemplate(final Class<V> clazz, RedisConnectionFactory redisConnectionFactory) {
//        // The generic type is changed to String Object, which is convenient to use
//        RedisTemplate<T, V> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        // Json serialization configuration
//        // Use json to parse the object
//        Jackson2JsonRedisSerializer<V> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(clazz);
//        ObjectMapper om = new ObjectMapper();
//        // Escaping through ObjectMapper
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//
//        // Serialization of String
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        // key uses String serialization
//        redisTemplate.setKeySerializer(stringRedisSerializer);
//        // The key of hash also uses String serialization
//        redisTemplate.setHashKeySerializer(stringRedisSerializer);
//        // The serialization method of value adopts jackson
//        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
//        // The value serialization of hash also uses jackson
//        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
//
//        // Initialize redisTemplate
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
}
