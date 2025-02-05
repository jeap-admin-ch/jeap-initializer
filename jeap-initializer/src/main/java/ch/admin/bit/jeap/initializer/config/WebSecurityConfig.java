package ch.admin.bit.jeap.initializer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/wizard/**", "/api/**", "/style.css").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
