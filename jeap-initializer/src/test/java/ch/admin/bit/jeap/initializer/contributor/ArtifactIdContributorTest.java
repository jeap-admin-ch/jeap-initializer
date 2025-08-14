package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactIdContributorTest {

    private final ArtifactIdContributor contributor = new ArtifactIdContributor();

    @TempDir
    private Path tempDir;

    @Test
    void contributeSingleModuleSrcMain() throws IOException {
        Path targetFile = Path.of(tempDir.toString(), "pom.xml");
        Files.copy(new ClassPathResource("./bit-jme-app-service/pom.xml").getInputStream(), targetFile);

        ProjectTemplate template = new ProjectTemplate();
        template.setArtifactId("bit-jme-app");
        ProjectRequest request = new ProjectRequest();
        request.setArtifactId("my-application");

        contributor.contribute(tempDir, request, template);

        String pom = Files.readString(Path.of(tempDir + "/pom.xml"));
        assertTrue(pom.contains("<artifactId>my-application</artifactId>"));
    }

    @Test
    void contributeMultiModuleFoldersAreRenamed() throws IOException {
        createDirectories(Path.of(tempDir.toString() + "/bit-jme-app-ui"));
        createDirectories(Path.of(tempDir + "/bit-jme-app-service"));


        ProjectTemplate template = new ProjectTemplate();
        template.setArtifactId("bit-jme-app");
        ProjectRequest request = new ProjectRequest();
        request.setArtifactId("my-application");

        contributor.contribute(tempDir, request, template);

        assertTrue(exists(Path.of( tempDir + "/my-application-ui")));
        assertTrue(exists(Path.of( tempDir + "/my-application-service")));
        assertFalse(exists(Path.of( tempDir + "/bit-jme-app-ui")));
        assertFalse(exists(Path.of( tempDir + "/bit-jme-app-service")));
    }

    @Test
    void contributeMultiModuleArtifactIdsAreRenamed() throws IOException {
        addTestFileToFolder("pom.xml", tempDir);
        createDirectories(Path.of(tempDir + "/bit-jme-app-service"));
        addTestFileToFolder("bit-jme-app-service/pom.xml", tempDir);
        createDirectories(Path.of(tempDir + "/bit-jme-app-ui"));
        addTestFileToFolder("bit-jme-app-ui/pom.xml", tempDir);


        ProjectTemplate template = new ProjectTemplate();
        template.setArtifactId("bit-jme-app");
        ProjectRequest request = new ProjectRequest();
        request.setArtifactId("my-application");

        contributor.contribute(tempDir, request, template);

        String parentPom = Files.readString(Path.of(tempDir + "/pom.xml"));
        assertTrue(parentPom.contains("<artifactId>my-application</artifactId>"));
        assertTrue(parentPom.contains("<module>my-application-ui</module>"));
        assertTrue(parentPom.contains("<module>my-application-service</module>"));

        String servicePom = Files.readString(Path.of(tempDir + "/my-application-service/pom.xml"));
        assertTrue(servicePom.contains("<artifactId>my-application</artifactId>"));
        assertTrue(servicePom.contains("<artifactId>my-application-service</artifactId>"));

        String uiPom = Files.readString(Path.of(tempDir + "/my-application-ui/pom.xml"));
        assertTrue(uiPom.contains("<artifactId>my-application</artifactId>"));
        assertTrue(uiPom.contains("<artifactId>my-application-ui</artifactId>"));
    }



    @Test
    void jenkinsFilesAreAdapted() throws IOException {
        addTestFileToFolder("Jenkinsfile.gitops-deployment", tempDir);
        addTestFileToFolder("Jenkinsfile", tempDir);

        ProjectTemplate template = new ProjectTemplate();
        template.setArtifactId("bit-jme-app");
        ProjectRequest request = new ProjectRequest();
        request.setArtifactId("my-application");

        contributor.contribute(tempDir, request, template);

        String gitOpsJenkinsFile = Files.readString(Path.of(tempDir + "/Jenkinsfile.gitops-deployment"));
        assertTrue(gitOpsJenkinsFile.contains("serviceName: 'my-application'"));
        assertFalse(gitOpsJenkinsFile.contains("bit-jme-app"));

        String jenkinsFile = Files.readString(Path.of(tempDir + "/Jenkinsfile"));
        assertTrue(jenkinsFile.contains("bit_jme.my-application.gitops-deployment"));
        assertFalse(jenkinsFile.contains("bit-jme-app"));
    }

    @Test
    void gitHubFilesAreAdapted() throws IOException {
        Path gitHubDirectory = tempDir.resolve(".github");
        createDirectories(gitHubDirectory);
        addTestFileToFolder("jeapDeployPipelineConfig.json", gitHubDirectory);

        ProjectTemplate template = new ProjectTemplate();
        template.setArtifactId("bit-jme-app");
        ProjectRequest request = new ProjectRequest();
        request.setArtifactId("my-application");

        contributor.contribute(tempDir, request, template);

        String gitOpsJenkinsFile = Files.readString(Path.of(gitHubDirectory + "/jeapDeployPipelineConfig.json"));
        assertTrue(gitOpsJenkinsFile.contains("\"serviceName\" : \"my-application\""));
        assertFalse(gitOpsJenkinsFile.contains("bit-jme-app"));
    }

}
