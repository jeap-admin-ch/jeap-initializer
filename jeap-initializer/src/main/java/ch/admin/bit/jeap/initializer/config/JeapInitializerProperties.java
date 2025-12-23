package ch.admin.bit.jeap.initializer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
@ConfigurationProperties(prefix = "jeap.initializer")
public class JeapInitializerProperties implements InitializingBean {

    private Duration templateCacheDuration = Duration.ofHours(4);

    private Pattern sourceFilesPattern = Pattern.compile("CODEOWNERS|Dockerfile|Jenkinsfile.*|(.+\\.(md|html|css|java|xml|yaml|yml|properties|json|conf|ts))", Pattern.CASE_INSENSITIVE);

    private Map<String, ProjectTemplateProperties> templates = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() {
        templates.keySet().forEach(templateKey -> {
            ProjectTemplateProperties template = templates.get(templateKey);
            Assert.notNull(template.getRepositoryConfiguration(), "'repositoryConfiguration' property block must be set for template %s.".formatted(templateKey));
            Assert.hasText(template.getRepositoryConfiguration().getUrl(), "'repositoryConfiguration.url' property must be set for template %s.".formatted(templateKey));
        });
    }
}
