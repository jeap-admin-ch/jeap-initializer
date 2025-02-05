package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static java.nio.file.Files.createDirectories;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationNameContributorTest {

    private static final String ORIGINAL_NAME = "Original Test Application Name";
    private static final String FINAL_NAME = "Final Application Name";

    private final ApplicationNameContributor contributor = new ApplicationNameContributor();

    @TempDir
    private Path tempDir;

    @Test
    void readmeFileIsAdapted() throws IOException {
        addTestFileToFolder("README.md", tempDir);

        ProjectTemplate template = new ProjectTemplate();
        template.setName(ORIGINAL_NAME);

        ProjectRequest request = new ProjectRequest();
        request.setApplicationName(FINAL_NAME);

        contributor.contribute(tempDir, request, template);

        String readmeMD = Files.readString(Path.of(tempDir + "/README.md"));
        assertTrue(readmeMD.contains(FINAL_NAME));
        assertFalse(readmeMD.contains(ORIGINAL_NAME));
    }

    @Test
    void indexFileIsAdapted() throws IOException {
        createDirectories(Path.of(tempDir + "/bit-jme-app-ui"));
        addTestFileToFolder("bit-jme-app-ui/index.html", tempDir);

        ProjectTemplate template = new ProjectTemplate();
        template.setName(ORIGINAL_NAME);

        ProjectRequest request = new ProjectRequest();
        request.setApplicationName(FINAL_NAME);

        contributor.contribute(tempDir, request, template);

        String indexHTML = Files.readString(Path.of(tempDir + "/bit-jme-app-ui/index.html"));
        assertTrue(indexHTML.contains(FINAL_NAME));
        assertFalse(indexHTML.contains(ORIGINAL_NAME));
    }


}
