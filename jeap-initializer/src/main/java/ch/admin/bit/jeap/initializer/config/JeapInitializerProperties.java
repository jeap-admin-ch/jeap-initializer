package ch.admin.bit.jeap.initializer.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Data
@ConfigurationProperties(prefix = "jeap.initializer")
public class JeapInitializerProperties implements InitializingBean {

    private Duration templateCacheDuration = Duration.ofHours(4);

    private Pattern sourceFilesPattern = Pattern.compile("CODEOWNERS|Dockerfile|Jenkinsfile.*|(.+\\.(md|html|css|java|xml|yaml|yml|properties|json|conf|ts))", Pattern.CASE_INSENSITIVE);

    private Map<String, PlatformProperties> platforms = new LinkedHashMap<>();
    private Map<String, ProjectTemplateProperties> templates = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() {
        platforms.keySet().forEach(platformKey -> {
            PlatformProperties platformProperties = platforms.get(platformKey);
            Assert.hasText(platformProperties.getName(), "'jeap.initializer.platforms' %s must have a platform name configured.".formatted(platformKey));
            Assert.hasText(platformProperties.getDescription(), "'jeap.initializer.platforms' %s must have a platform description configured.".formatted(platformKey));
        });
        log.info("jEAP initializer has {} platform(s) configured: {}", platforms.size(), StringUtils.collectionToCommaDelimitedString(platforms.keySet()));

        templates.keySet().forEach(templateKey -> {
            ProjectTemplateProperties template = templates.get(templateKey);
            Assert.notNull(template.getRepositoryConfiguration(), "'repositoryConfiguration' property block must be set for template %s.".formatted(templateKey));
            Assert.hasText(template.getRepositoryConfiguration().getUrl(), "'repositoryConfiguration.url' property must be set for template %s.".formatted(templateKey));
        });
        log.info("jEAP initializer has {} template(s) configured: {}", templates.size(), StringUtils.collectionToCommaDelimitedString(templates.keySet()));
    }
}
