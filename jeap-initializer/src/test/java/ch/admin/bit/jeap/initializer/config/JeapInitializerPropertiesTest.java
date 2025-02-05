package ch.admin.bit.jeap.initializer.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JeapInitializerPropertiesTest {

    @Test
    void templateIsConfigured() {
        JeapInitializerProperties jeapInitializerProperties = createConfigurationProperties(env -> {
            env.withProperty("jeap.initializer.templates.jeap-scs.repositoryConfiguration.url", "https://some-template.git");
            env.withProperty("jeap.initializer.templates.jeap-scs.repositoryConfiguration.reference", "my-branch");
            env.withProperty("jeap.initializer.templates.jeap-scs.repositoryConfiguration.user", "myUser");
            env.withProperty("jeap.initializer.templates.jeap-scs.repositoryConfiguration.password", "myPass");
            return env;
        });

        ProjectTemplateProperties jeapTemplate = jeapInitializerProperties.getTemplates().get("jeap-scs");
        assertThat(jeapTemplate.getRepositoryConfiguration().getUrl()).isEqualTo("https://some-template.git");
        assertThat(jeapTemplate.getRepositoryConfiguration().getReference()).isEqualTo("my-branch");
        assertThat(jeapTemplate.getRepositoryConfiguration().getUser()).isEqualTo("myUser");
        assertThat(jeapTemplate.getRepositoryConfiguration().getPassword()).isEqualTo("myPass");
    }

    @Test
    void gitOpsIsConfigured() {
        JeapInitializerProperties jeapInitializerProperties = createConfigurationProperties(env -> {
            env.withProperty("jeap.initializer.templates.jeap-scs.gitOpsRepositoryConfiguration.url", "https://some-gitops-template.git");
            env.withProperty("jeap.initializer.templates.jeap-scs.gitOpsRepositoryConfiguration.reference", "my-gitops-branch");
            env.withProperty("jeap.initializer.templates.jeap-scs.gitOpsRepositoryConfiguration.user", "myUserForGitOps");
            env.withProperty("jeap.initializer.templates.jeap-scs.gitOpsRepositoryConfiguration.password", "myPassForGitOps");
            return env;
        });

        ProjectTemplateProperties jeapTemplate = jeapInitializerProperties.getTemplates().get("jeap-scs");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getUrl()).isEqualTo("https://some-gitops-template.git");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getReference()).isEqualTo("my-gitops-branch");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getUser()).isEqualTo("myUserForGitOps");
        assertThat(jeapTemplate.getGitOpsRepositoryConfiguration().getPassword()).isEqualTo("myPassForGitOps");
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
