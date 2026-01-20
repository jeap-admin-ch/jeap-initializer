package ch.admin.bit.jeap.initializer.template;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.config.PlatformProperties;
import ch.admin.bit.jeap.initializer.config.ProjectTemplateProperties;
import ch.admin.bit.jeap.initializer.git.GitService;
import ch.admin.bit.jeap.initializer.model.GitRepositoryConfiguration;
import ch.admin.bit.jeap.initializer.model.Platform;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingTemplateRepositoryTest {

    @Mock
    private JeapInitializerProperties initializerProperties;

    @Mock
    private GitService gitService;

    private CachingTemplateRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CachingTemplateRepository(initializerProperties, gitService);
    }

    @Test
    void getTemplateKeys_returnsCopyOfTemplateKeys() {
        Map<String, ProjectTemplateProperties> templates = new LinkedHashMap<>();
        templates.put("tpl-a", new ProjectTemplateProperties());
        templates.put("tpl-b", new ProjectTemplateProperties());
        when(initializerProperties.getTemplates()).thenReturn(templates);

        Set<String> keys = repository.getTemplateKeys();

        assertThat(keys).containsExactlyInAnyOrder("tpl-a", "tpl-b");
        // Ensure it is a copy (immutable) by verifying mutation on source does not affect returned set
        templates.put("tpl-c", new ProjectTemplateProperties());
        assertThat(keys).doesNotContain("tpl-c");
    }

    @Test
    void getTemplate_returnsNull_whenKeyNotFound() {
        when(initializerProperties.getTemplates()).thenReturn(new LinkedHashMap<>());
        ProjectTemplate tpl = repository.getTemplate("missing");
        assertThat(tpl).isNull();
        verifyNoInteractions(gitService);
    }

    @Test
    void getTemplate_loadsYamlAndPopulatesFields_andValidatesRequiredProperties() throws Exception {
        // Arrange properties
        GitRepositoryConfiguration repoCfg = new GitRepositoryConfiguration();
        repoCfg.setUrl("https://host/scm/repo.git");
        repoCfg.setReference("main");
        GitRepositoryConfiguration gitOpsCfg = new GitRepositoryConfiguration();
        gitOpsCfg.setUrl("https://host/scm/gitops.git");
        gitOpsCfg.setReference("master");

        ProjectTemplateProperties templateProps = new ProjectTemplateProperties();
        templateProps.setRepositoryConfiguration(repoCfg);
        templateProps.setGitOpsRepositoryConfiguration(gitOpsCfg);

        Map<String, ProjectTemplateProperties> templates = new LinkedHashMap<>();
        templates.put("tpl-key", templateProps);
        when(initializerProperties.getTemplates()).thenReturn(templates);

        // YAML content that satisfies required fields
        String yaml = """
                name: My Template
                description: Some desc
                base-package: ch.pkg
                artifact-id: my-artifact
                group-id: ch.admin.bit
                system-name: sys
                department: BIT
                platform: some-platform
                template-modules:
                  - id: module-a
                    name: Module A
                    description: Desc A
                """;
        when(gitService.getFileContentFromRepository(repoCfg, "initializer.yaml")).thenReturn(yaml);

        // Act
        ProjectTemplate tpl = repository.getTemplate("tpl-key");

        // Assert
        assertThat(tpl).isNotNull();
        assertThat(tpl.getKey()).isEqualTo("tpl-key");
        assertThat(tpl.getName()).isEqualTo("My Template");
        assertThat(tpl.getDescription()).isEqualTo("Some desc");
        assertThat(tpl.getBasePackage()).isEqualTo("ch.pkg");
        assertThat(tpl.getArtifactId()).isEqualTo("my-artifact");
        assertThat(tpl.getGroupId()).isEqualTo("ch.admin.bit");
        assertThat(tpl.getSystemName()).isEqualTo("sys");
        assertThat(tpl.getDepartment()).isEqualTo("BIT");
        assertThat(tpl.getRepositoryConfiguration()).isSameAs(repoCfg);
        assertThat(tpl.getPlatform()).isEqualTo("some-platform");
        assertThat(tpl.getGitOpsRepositoryConfiguration()).isSameAs(gitOpsCfg);
        assertThat(tpl.getAllModuleIds()).containsExactly("module-a");
        TemplateModule moduleA = tpl.getTemplateModule("module-a");
        assertThat(moduleA.getName()).isEqualTo("Module A");
    }

    @Test
    void getTemplate_throwsTemplateException_whenYamlLoadFails() throws Exception {
        GitRepositoryConfiguration repoCfg = new GitRepositoryConfiguration();
        repoCfg.setUrl("https://host/scm/repo.git");
        ProjectTemplateProperties templateProps = new ProjectTemplateProperties();
        templateProps.setRepositoryConfiguration(repoCfg);
        Map<String, ProjectTemplateProperties> templates = new LinkedHashMap<>();
        templates.put("tpl-key", templateProps);
        when(initializerProperties.getTemplates()).thenReturn(templates);

        when(gitService.getFileContentFromRepository(repoCfg, "initializer.yaml"))
                .thenThrow(new java.io.IOException("IO problem"));

        assertThrows(TemplateException.class, () -> repository.getTemplate("tpl-key"));
    }

    @Test
    void getConfiguredPlatform_returnsFallback_whenKeyUnknown() {
        Map<String, PlatformProperties> platforms = new LinkedHashMap<>();
        platforms.put("another", new PlatformProperties());
        when(initializerProperties.getPlatforms()).thenReturn(platforms);
        Platform platform = repository.getConfiguredPlatform("platform-key");
        assertThat(platform.key()).isEqualTo("platform-key");
        assertThat(platform.name()).isEqualTo("platform-key");
        assertThat(platform.description()).isEmpty();
    }

    @Test
    void getPlatform_populatesFields_andValidatesNamePresent() {
        PlatformProperties platformProperties = new PlatformProperties();
        platformProperties.setName("My App");
        platformProperties.setDescription("Some description");
        Map<String, PlatformProperties> platforms = new LinkedHashMap<>();
        platforms.put("platform-key", platformProperties);
        when(initializerProperties.getPlatforms()).thenReturn(platforms);

        Platform platform = repository.getConfiguredPlatform("platform-key");
        assertThat(platform).isNotNull();
        assertThat(platform.key()).isEqualTo("platform-key");
        assertThat(platform.name()).isEqualTo("My App");
        assertThat(platform.description()).isEqualTo("Some description");
    }
}

