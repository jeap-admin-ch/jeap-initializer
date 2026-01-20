package ch.admin.bit.jeap.initializer.template;


import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.config.PlatformProperties;
import ch.admin.bit.jeap.initializer.config.ProjectTemplateProperties;
import ch.admin.bit.jeap.initializer.git.GitService;
import ch.admin.bit.jeap.initializer.model.Platform;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Set;

/**
 * Provides access to {@link ProjectTemplate} entities. The entities contain project template metadata such as name,
 * description and parameters, and are loaded from the template's git repositories.
 * <p>
 * Project template information loaded from repositories are cached for a certain time to avoid cloning the template
 * repositories every time templates information is required.
 */
@Slf4j
@AllArgsConstructor
@Component
class CachingTemplateRepository implements TemplateRepository {

    private final JeapInitializerProperties initializerProperties;
    private final GitService gitService;
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    public Set<String> getTemplateKeys() {
        return Set.copyOf(initializerProperties.getTemplates().keySet());
    }

    @Cacheable("templateListCache")
    public ProjectTemplate getTemplate(String key) {
        return loadTemplate(key);
    }

    @Override
    public Platform getConfiguredPlatform(String key) {
        PlatformProperties platformProperties = initializerProperties.getPlatforms().get(key);
        if (platformProperties == null) {
            log.error("Platform with key: [{}] does not exist in the configuration.", key);
            // Return fallback platform metadata to be ablet o select templates for this platform
            return new Platform(key, key, "");
        }

        return createPlatform(key, platformProperties);
    }

    private ProjectTemplate loadTemplate(String key) {
        ProjectTemplateProperties templateProperties = initializerProperties.getTemplates().get(key);
        if (templateProperties == null) {
            return null;
        }

        ProjectTemplate template = loadTemplateContent(templateProperties);
        template.setKey(key);
        template.setRepositoryConfiguration(templateProperties.getRepositoryConfiguration());
        template.setGitOpsRepositoryConfiguration(templateProperties.getGitOpsRepositoryConfiguration());

        Assert.hasText(template.getName(), "'name' property must be set for template %s.".formatted(key));
        Assert.hasText(template.getBasePackage(), "'basePackage' property must be set for template %s.".formatted(key));
        Assert.hasText(template.getSystemName(), "'systemName' property must be set for template %s.".formatted(key));
        Assert.hasText(template.getArtifactId(), "'artifactId' property must be set for template %s.".formatted(key));
        Assert.hasText(template.getGroupId(), "'groupId' property must be set for template %s.".formatted(key));
        Assert.hasText(template.getPlatform(), "'platform' property must be set for template %s.".formatted(key));

        return template;
    }

    private ProjectTemplate loadTemplateContent(ProjectTemplateProperties templateProperties) {
        try {
            String content = gitService.getFileContentFromRepository(templateProperties.getRepositoryConfiguration(), "initializer.yaml");
            return objectMapper.readValue(content, ProjectTemplate.class);
        } catch (IOException cause) {
            throw TemplateException.templateLoadingFailed(cause);
        }
    }

    private static Platform createPlatform(String key, PlatformProperties platformProperties) {
        return new Platform(key, platformProperties.getName(), platformProperties.getDescription());
    }
}
