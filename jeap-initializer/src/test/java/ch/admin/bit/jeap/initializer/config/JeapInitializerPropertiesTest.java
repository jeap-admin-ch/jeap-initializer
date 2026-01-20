package ch.admin.bit.jeap.initializer.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JeapInitializerPropertiesTest {

    public static final String PLATFORM_1_KEY = "platform1";
    public static final String PLATFORM_1_NAME = "Platform 1";
    public static final String PLATFORM_1_DESCRIPTION = "Platform 1 Description";
    public static final String PLATFORM_1_URL = "https://repo/git.git";

    @Test
    void templateIsConfigured() {
        JeapInitializerProperties jeapInitializerProperties = createConfigurationProperties(env -> {
            env.withProperty("jeap.initializer.templates.platform1.repositoryConfiguration.url", "https://some-template.git");
            env.withProperty("jeap.initializer.templates.platform1.repositoryConfiguration.reference", "my-branch");
            env.withProperty("jeap.initializer.templates.platform1.repositoryConfiguration.user", "myUser");
            env.withProperty("jeap.initializer.templates.platform1.repositoryConfiguration.password", "myPass");
            return env;
        });

        ProjectTemplateProperties jeapTemplate = jeapInitializerProperties.getTemplates().get(PLATFORM_1_KEY);
        assertThat(jeapTemplate.getRepositoryConfiguration().getUrl()).isEqualTo("https://some-template.git");
        assertThat(jeapTemplate.getRepositoryConfiguration().getReference()).isEqualTo("my-branch");
        assertThat(jeapTemplate.getRepositoryConfiguration().getUser()).isEqualTo("myUser");
        assertThat(jeapTemplate.getRepositoryConfiguration().getPassword()).isEqualTo("myPass");
    }

    @Test
    void gitOpsIsConfigured() {
        JeapInitializerProperties jeapInitializerProperties = createConfigurationProperties(env -> {
            env.withProperty("jeap.initializer.templates.platform1.gitOpsRepositoryConfiguration.url", "https://some-gitops-template.git");
            env.withProperty("jeap.initializer.templates.platform1.gitOpsRepositoryConfiguration.reference", "my-gitops-branch");
            env.withProperty("jeap.initializer.templates.platform1.gitOpsRepositoryConfiguration.user", "myUserForGitOps");
            env.withProperty("jeap.initializer.templates.platform1.gitOpsRepositoryConfiguration.password", "myPassForGitOps");
            return env;
        });

        ProjectTemplateProperties jeapTemplate = jeapInitializerProperties.getTemplates().get(PLATFORM_1_KEY);
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getUrl()).isEqualTo("https://some-gitops-template.git");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getReference()).isEqualTo("my-gitops-branch");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getUser()).isEqualTo("myUserForGitOps");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getPassword()).isEqualTo("myPassForGitOps");
    }

    @Test
    void applicationsAreConfigured_andValidatedOnAfterPropertiesSet() {
        JeapInitializerProperties props = createConfigurationProperties(env -> {
            env.withProperty("jeap.initializer.platforms.platform1.name", PLATFORM_1_NAME);
            env.withProperty("jeap.initializer.platforms.platform1.description", PLATFORM_1_DESCRIPTION);
            env.withProperty("jeap.initializer.templates.platform1-template1.repositoryConfiguration.url", PLATFORM_1_URL);
            env.withProperty("jeap.initializer.templates.platform1-template1.repositoryConfiguration.reference", "master");
            return env;
        });

        // Binding checks
        PlatformProperties platform1 = props.getPlatforms().get(PLATFORM_1_KEY);
        assertThat(platform1.getName()).isEqualTo(PLATFORM_1_NAME);

        // Validation should pass when required fields exist
        props.afterPropertiesSet();
    }

    @Test
    void afterPropertiesSet_throws_whenPlatformNameMissing() {
        JeapInitializerProperties props = createConfigurationProperties(env -> {
            env.withProperty("jeap.initializer.platforms.platform1.description", "Desc only, no name");
            env.withProperty("jeap.initializer.platforms.platform1.description", PLATFORM_1_DESCRIPTION);
            return env;
        });

        org.assertj.core.api.Assertions.assertThatThrownBy(props::afterPropertiesSet)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must have a platform name");
    }

    @Test
    void afterPropertiesSet_throws_whenTemplateRepositoryUrlMissing() {
        JeapInitializerProperties props = createConfigurationProperties(env -> {
            // repositoryConfiguration present but url missing
            env.withProperty("jeap.initializer.templates.tpl1.repositoryConfiguration.reference", "main");
            env.withProperty("jeap.initializer.platforms.platform1.name", PLATFORM_1_NAME);
            env.withProperty("jeap.initializer.platforms.platform1.description", PLATFORM_1_DESCRIPTION);
            return env;
        });

        org.assertj.core.api.Assertions.assertThatThrownBy(props::afterPropertiesSet)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'repositoryConfiguration.url' property must be set for template tpl1");
    }

    private static JeapInitializerProperties createConfigurationProperties(Function<MockEnvironment, MockEnvironment> environmentFunction) {
        MockEnvironment environment = environmentFunction.apply(new MockEnvironment());
        JeapInitializerProperties kafkaProperties = new JeapInitializerProperties();
        Bindable<JeapInitializerProperties> propertiesBindable = Bindable.ofInstance(kafkaProperties);
        return Binder.get(environment)
                .bind("jeap.initializer", propertiesBindable)
                .get();
    }

}
