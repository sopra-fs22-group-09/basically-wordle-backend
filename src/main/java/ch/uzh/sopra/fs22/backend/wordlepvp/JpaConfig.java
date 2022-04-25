package ch.uzh.sopra.fs22.backend.wordlepvp;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
@EnableJpaAuditing
public class JpaConfig {
}
