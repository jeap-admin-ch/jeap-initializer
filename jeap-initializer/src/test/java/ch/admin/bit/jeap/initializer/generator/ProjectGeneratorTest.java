package ch.admin.bit.jeap.initializer.generator;

import ch.admin.bit.jeap.initializer.config.TemplateParameterMissingException;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.SelectedModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The files for the template are generated in {@link ch.admin.bit.jeap.initializer.TestConfig#testGitService()}
 */
@SpringBootTest
@ActiveProfiles("test")
class ProjectGeneratorTest {

    @Autowired
    private ProjectGenerator projectGenerator;

    @TempDir
    Path tempDir;

    @Test
    void generate() throws IOException {
        ProjectRequest projectRequest = getProjectRequest();
        projectGenerator.generate(projectRequest, tempDir);

        // Test initial git generated content
        assertThat(tempDir.resolve("README.md")).content()
                .isEqualTo("This is the readme file");

        // Test FolderRenamerContributor
        Path applicationFile = tempDir.resolve("src/main/java/ch/some/pkg/SampleApplication.java");
        assertThat(applicationFile).exists();

        // Test JavaPackageRenamerContributor
        assertThat(applicationFile).content()
                .contains("package ch.some.pkg;")
                .contains("import ch.some.pkg.something;");

        // Assert an empty Git repository has been initialized
        assertThat(tempDir.toAbsolutePath().resolve(".git")).exists();
        assertThat(tempDir.toAbsolutePath().resolve(".git/config")).exists();

        // Test that a module that has not been selected has been removed
        assertThat(applicationFile).content()
                .doesNotContain("START MODULE object-storage")
                .doesNotContain("objectStorageMethod");
        assertThat(tempDir.resolve("pom-module-test.xml")).content()
                .doesNotContain("START MODULE object-storage")
                .doesNotContain("jeap-spring-boot-object-storage-starter");
        Path moduleSpecificFile = tempDir.resolve("src/main/java/ch/some/pkg/ModuleSpecificFile.java");
        assertThat(moduleSpecificFile).doesNotExist();
    }

    @Test
    void generate_missingParameter() {
        ProjectRequest projectRequest = getProjectRequest();
        projectRequest.getTemplateParameters().clear();
        assertThatThrownBy(() -> projectGenerator.generate(projectRequest, tempDir))
                .isInstanceOf(TemplateParameterMissingException.class)
                .hasMessageContaining("templateParameter1");
    }

    @Test
    void generate_withSelectedModule() throws IOException {
        ProjectRequest projectRequest = getProjectRequest();
        SelectedModule selectedModule = new SelectedModule();
        selectedModule.setId("object-storage");
        selectedModule.setModuleParameters(Map.of("moduleParameter1", "value"));
        projectRequest.getSelectedTemplateModules().add(selectedModule);
        projectGenerator.generate(projectRequest, tempDir);

        // Test that a module that has been selected is present, and the surrounding tags have been removed
        assertThat(tempDir.resolve("pom-module-test.xml")).content()
                .doesNotContain("START MODULE object-storage")
                .contains("jeap-spring-boot-object-storage-starter");
        Path applicationFile = tempDir.resolve("src/main/java/ch/some/pkg/SampleApplication.java");
        assertThat(applicationFile).content()
                .doesNotContain("START MODULE object-storage")
                .contains("objectStorageMethod");
        Path moduleSpecificFile = tempDir.resolve("src/main/java/ch/some/pkg/ModuleSpecificFile.java");
        assertThat(moduleSpecificFile).content()
                .isEqualToIgnoringNewLines("""
                        package ch.some.pkg;
                        
                        public class ModuleSpecificFile {
                        }
                        """);
    }

    @Test
    void generate_withSelectedModule_missingParameter() throws IOException {
        ProjectRequest projectRequest = getProjectRequest();
        SelectedModule selectedModule = new SelectedModule();
        selectedModule.setId("object-storage");
        projectRequest.getSelectedTemplateModules().add(selectedModule);
        assertThatThrownBy(() -> projectGenerator.generate(projectRequest, tempDir))
                .isInstanceOf(TemplateParameterMissingException.class)
                .hasMessageContaining("moduleParameter1");
    }

    @Test
    void generateWithGitOpsConfiguration() throws IOException {
        ProjectRequest projectRequest = getProjectRequest();
        projectRequest.setTemplate("gitops-template");
        projectGenerator.generate(projectRequest, tempDir);

        // Test initial git generated content
        assertThat(tempDir.resolve("README.md"))
                .content().contains("This is the readme file");

        // Test GitOps Repo has been retrieved as well
        assertEquals("This file comes from the GitOps repo", readString(Path.of(tempDir + "/gitops/gitops-file.txt")));
    }

    private static ProjectRequest getProjectRequest() {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setBasePackage("ch.some.pkg");
        Map<String, String> templateParameters = new HashMap<>();
        templateParameters.put("templateParameter1", "value1");
        templateParameters.put("templateParameter2", "value2");
        projectRequest.setTemplateParameters(templateParameters);
        return projectRequest;
    }
}
