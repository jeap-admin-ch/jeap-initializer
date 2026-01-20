package ch.admin.bit.jeap.initializer.ui;

import ch.admin.bit.jeap.initializer.config.WebSecurityConfig;
import ch.admin.bit.jeap.initializer.model.*;
import ch.admin.bit.jeap.initializer.template.TemplateRepository;
import ch.admin.bit.jeap.initializer.template.TemplateService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

@TestConfiguration
@Import({ProjectRequestFactory.class, ModelConversionConfig.class, WebSecurityConfig.class})
class WizardControllerTestConfig {
    @Bean
    TemplateService templateService() {
        return new TemplateService(new TemplateRepository() {

            @Override
            public Set<String> getTemplateKeys() {
                return Set.of("test-template", "other-template");
            }

            @Override
            public ProjectTemplate getTemplate(String key) {
                ProjectTemplate template = new ProjectTemplate();
                template.setKey("test-template");
                template.setName("Test Template");
                template.setDescription("Description of the test template");
                template.setPlatform("test-platform");
                // Ensure repository configuration is present to satisfy TemplateService filtering
                GitRepositoryConfiguration repoCfg = new GitRepositoryConfiguration();
                repoCfg.setUrl("https://host/scm/" + key + "/repo.git");
                template.setRepositoryConfiguration(repoCfg);

                TemplateParameter templateParameter = new TemplateParameter("awsAccountId", "AWS Account ID", "ID of the AWS account");
                template.getTemplateParameters().add(templateParameter);
                TemplateModule templateModule = new TemplateModule();
                templateModule.setId("test-module");
                templateModule.setName("Test Module");
                templateModule.setDescription("Description of the test module");
                templateModule.getModuleParameters().add(new TemplateParameter("bucketName", "Bucket Name", "Name of the S3 bucket"));
                template.setTemplateModules(List.of(templateModule));
                return template;
            }

            @Override
            public Platform getConfiguredPlatform(String key) {
                return new Platform(key, "App One", "Description for App One");
            }
        });
    }
}
