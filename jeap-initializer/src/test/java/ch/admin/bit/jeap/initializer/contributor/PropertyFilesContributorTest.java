package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static java.nio.file.Files.createDirectories;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyFilesContributorTest {

    private static final String ORIGINAL_SYSTEM_NAME = "jme";
    private static final String ORIGINAL_ARTIFACT_ID = "my-app";
    private static final String FINAL_SYSTEM_NAME = "new-system";
    private static final String FINAL_ARTIFACT_ID = "new-app";

    private static final String ORIGINAL_CONTEXT_PATH = "jme-jeap-nivel-quadrel-project-template";
    private static final String FINAL_CONTEXT_PATH = FINAL_ARTIFACT_ID;

    private final PropertyFilesContributor contributor = new PropertyFilesContributor();

    @TempDir
    private Path tempDir;

    @Test
    void propertyFilesAreAdapted() throws IOException {
        createDirectories(Path.of(tempDir + "/bit-jme-app-service/src/main/resources"));
        addTestFileToFolder("bit-jme-app-service/src/main/resources/application.yml", tempDir);
        addTestFileToFolder("bit-jme-app-service/src/main/resources/application.yaml", tempDir);
        addTestFileToFolder("bit-jme-app-service/src/main/resources/application.properties", tempDir);

        ProjectTemplate template = new ProjectTemplate();

        template.setTemplateParameters(
                List.of(new TemplateParameter("1", "applicationContextPath", ORIGINAL_CONTEXT_PATH))
        );
        template.setSystemName(ORIGINAL_SYSTEM_NAME);
        template.setArtifactId(ORIGINAL_ARTIFACT_ID);

        ProjectRequest request = new ProjectRequest();
        request.setTemplateParameters(Map.of("applicationContextPath", FINAL_CONTEXT_PATH));
        request.setSystemName(FINAL_SYSTEM_NAME);
        request.setArtifactId(FINAL_ARTIFACT_ID);

        contributor.contribute(tempDir, request, template);

        String applicationYml = Files.readString(Path.of(tempDir + "/bit-jme-app-service/src/main/resources/application.yml"));

        //Assert SystemName is adapted
        assertTrue(applicationYml.contains(FINAL_SYSTEM_NAME));
        assertFalse(applicationYml.contains(ORIGINAL_SYSTEM_NAME));

        //Assert ArtifactId is adapted
        assertTrue(applicationYml.contains(FINAL_ARTIFACT_ID));
        assertFalse(applicationYml.contains(ORIGINAL_ARTIFACT_ID));

        //Assert ContextPath is adapted
        assertTrue(applicationYml.contains(FINAL_CONTEXT_PATH));
        assertFalse(applicationYml.contains(ORIGINAL_CONTEXT_PATH));

        String applicationYaml = Files.readString(Path.of(tempDir + "/bit-jme-app-service/src/main/resources/application.yml"));
        assertTrue(applicationYml.contains(FINAL_SYSTEM_NAME));
        assertFalse(applicationYaml.contains(ORIGINAL_SYSTEM_NAME));

        String applicationProperties = Files.readString(Path.of(tempDir + "/bit-jme-app-service/src/main/resources/application.properties"));
        assertTrue(applicationProperties.contains("jeap.security.oauth2.resourceserver.system-name: " + FINAL_SYSTEM_NAME));
        assertFalse(applicationProperties.contains(ORIGINAL_SYSTEM_NAME));
    }

    @Test
    void environmentFilesSystemNameisAdapted() throws IOException {
        createDirectories(Path.of(tempDir + "/bit-jme-app-ui/src/environments"));
        addTestFileToFolder("bit-jme-app-ui/src/environments/environment.prod.ts", tempDir);
        addTestFileToFolder("bit-jme-app-ui/src/environments/environment.ts", tempDir);

        ProjectTemplate template = new ProjectTemplate();
        template.setArtifactId(ORIGINAL_ARTIFACT_ID);
        template.setSystemName(ORIGINAL_SYSTEM_NAME);

        ProjectRequest request = new ProjectRequest();
        request.setArtifactId(FINAL_ARTIFACT_ID);
        request.setSystemName(FINAL_SYSTEM_NAME);


        contributor.contribute(tempDir, request, template);

        String applicationYml = Files.readString(Path.of(tempDir + "/bit-jme-app-ui/src/environments/environment.ts"));
        assertTrue(applicationYml.contains(FINAL_SYSTEM_NAME));
        assertFalse(applicationYml.contains(ORIGINAL_SYSTEM_NAME));

        String applicationYaml = Files.readString(Path.of(tempDir + "/bit-jme-app-ui/src/environments/environment.prod.ts"));
        assertTrue(applicationYml.contains(FINAL_SYSTEM_NAME));
        assertFalse(applicationYaml.contains(ORIGINAL_SYSTEM_NAME));
    }

}
