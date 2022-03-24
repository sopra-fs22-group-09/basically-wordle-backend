package ch.uzh.sopra.fs22.backend.wordlepvp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .csrf(spec -> spec.disable())
                // Demonstrate that method security works
                // Best practice to use both for defense in depth
                .authorizeExchange(requests -> requests.anyExchange().permitAll())
                .logout(spec -> spec.disable())
                //.httpBasic(spec -> spec.disable())
                .cors(spec -> spec.disable())
                .build();
    }
}
