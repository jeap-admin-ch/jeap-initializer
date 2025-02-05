package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JavaPackageRenamerContributorTest {

    @TempDir
    static Path tempDir;

    private JavaPackageRenamerContributor contributor = new JavaPackageRenamerContributor();

    @Test
    void renamePackages() throws IOException {
        Path inputFile = Path.of(tempDir.toString(), "SampleApplication.java");
        Files.copy(new ClassPathResource("./SampleApplication.java").getInputStream(), inputFile);

        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("my.custom.app"), TestUtils.templateWithBasePackage("ch.admin.bit.jme"));

        String content = Files.readString(inputFile);
        assertTrue(content.contains("package my.custom.app;"));
        assertTrue(content.contains("import my.custom.app.something;"));
    }
}