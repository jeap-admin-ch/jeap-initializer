package ch.admin.bit.jeap.initializer;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

@EnableConfigurationProperties(JeapInitializerProperties.class)
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@Slf4j
public class Application {

    public static void main(String[] args) {

        Environment env = SpringApplication.run(Application.class, args).getEnvironment();
        String baseUrl = "http://localhost:" + env.getProperty("server.port") + env.getProperty("server.servlet.context-path");

        log.info("""
                        ----------------------------------------------------------
                        \t{} is running!
                        \tInitializer UI: {}
                        \tSwagger UI: \t{}/swagger-ui.html?urls.primaryName=jEAP%20Initializer%20Resources
                        \tProfile(s): \t{}
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                baseUrl,
                baseUrl,
                env.getActiveProfiles());
    }

}
