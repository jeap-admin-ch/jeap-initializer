package ch.admin.bit.jeap.initializer.contributor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class TemplateFileRenamingContributorTest {

    private static final String FILE_NAME = "jeapBuildPipelineConfig.json";
    private static final String TEMPLATE_FILE_NAME = FILE_NAME + ".initializer-template";

    @TempDir
    Path tempDir;

    private TemplateFileRenamingContributor contributor = new TemplateFileRenamingContributor();

    @Test
    void contribute_when_replacing() throws IOException {
        copyResourceToTemp(FILE_NAME, tempDir.resolve(FILE_NAME));
        copyResourceToTemp(TEMPLATE_FILE_NAME, tempDir.resolve(TEMPLATE_FILE_NAME));

        contributor.contribute(tempDir, null, null);

        String content = Files.readString(tempDir.resolve(FILE_NAME));
        assertFalse(content.contains("master"));
    }

    @Test
    void contribute_when_replacing_in_subfolder() throws IOException {
        Path subfolder = tempDir.resolve(".github");
        Files.createDirectories(subfolder);
        copyResourceToTemp(FILE_NAME, subfolder.resolve(FILE_NAME));
        copyResourceToTemp(TEMPLATE_FILE_NAME, subfolder.resolve(TEMPLATE_FILE_NAME));

        contributor.contribute(tempDir, null, null);

        String content = Files.readString(subfolder.resolve(FILE_NAME));
        assertFalse(content.contains("master"));
    }

    @Test
    void contribute_when_not_replacing() throws IOException {
        copyResourceToTemp(TEMPLATE_FILE_NAME, tempDir.resolve(TEMPLATE_FILE_NAME));

        contributor.contribute(tempDir, null, null);

        String content = Files.readString(tempDir.resolve(FILE_NAME));
        assertFalse(content.contains("master"));
    }

    @Test
    void contribute_when_not_replacing_in_subfolder() throws IOException {
        Path subfolder = tempDir.resolve(".github");
        Files.createDirectories(subfolder);
        copyResourceToTemp(TEMPLATE_FILE_NAME, subfolder.resolve(TEMPLATE_FILE_NAME));

        contributor.contribute(tempDir, null, null);

        String content = Files.readString(subfolder.resolve(FILE_NAME));
        assertFalse(content.contains("master"));
    }

    private void copyResourceToTemp(String resourceFileName, Path targetPath) throws IOException {
        try (InputStream resourceStream = getClass().getClassLoader()
                .getResourceAsStream(resourceFileName)) {
            if (resourceStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceFileName);
            }
            Files.copy(resourceStream, targetPath);
        }
    }
}
