package ch.admin.bit.jeap.initializer.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "jEAP Initializer API",
                description = "jEAP Initializer API",
                version = "v1"
        )
)
@Configuration
public class OpenApiConfig {
    @Bean
    GroupedOpenApi externalApi() {
        return GroupedOpenApi.builder()
                .group("jEAP Initializer Resources")
                .pathsToMatch("/api/**")
                .build();
    }

}
