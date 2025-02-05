package ch.admin.bit.jeap.initializer;

import ch.admin.bit.jeap.initializer.git.GitService;
import ch.admin.bit.jeap.initializer.model.GitRepositoryConfiguration;
import ch.admin.bit.jeap.initializer.util.FileProcessingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static ch.admin.bit.jeap.initializer.TestUtils.writeToFile;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public GitService testGitService() {
        return new GitService() {
            @Override
            public void cloneRepositoryAtPath(GitRepositoryConfiguration configuration, Path localPath) {
                try {
                    if (configuration.getUrl().contains("testhost")) {
                        writeToFile(localPath, "README.md", "This is the readme file");
                        addTestFileToFolder("pom-module-test.xml", localPath);
                        Path targetDirectory = localPath.resolve("src/main/java/ch/admin/bit/jme");
                        Files.createDirectories(targetDirectory);
                        addTestFileToFolder("SampleApplication.java", targetDirectory);
                        addTestFileToFolder("ModuleSpecificFile.java", targetDirectory);
                    }
                    if (configuration.getUrl().contains("github")) {
                        writeToFile(localPath, "gitops-file.txt", "This file comes from the GitOps repo");
                    }
                } catch (IOException e) {
                    throw FileProcessingException.ioException(e);
                }
            }

            @Override
            public String getFileContentFromRepository(GitRepositoryConfiguration configuration, String filePath) {
                if (configuration.getUrl().contains("github")) {
                    return """
                            name: "My template with GitOps"
                            description: "Some description"
                            base-package: "ch.admin.bit.jme"
                            system-name: "jme"
                            artifact-id: "my-app"
                            group-id: "ch.admin.bit.jeap"
                            """;
                }
                return """
                        name: "My template"
                        description: "Some description"
                        base-package: "ch.admin.bit.jme"
                        system-name: "jme"
                        artifact-id: "my-app"
                        group-id: "ch.admin.bit.jeap"
                        
                        template-modules:
                            - id: object-storage
                              name: Object Storage Module
                              description: Test module
                              module-parameters:
                                - id: moduleParameter1
                                  name: Module Parameter 1
                                  description: Test parameter for module
                        
                        template-parameters:
                          - id: templateParameter1
                            name: Template Parameter 1
                            description: Description of templateParameter1
                          - id: templateParameter2
                            name: Template Parameter 2
                            description: Description of templateParameter2
                        """;
            }
        };
    }
}
