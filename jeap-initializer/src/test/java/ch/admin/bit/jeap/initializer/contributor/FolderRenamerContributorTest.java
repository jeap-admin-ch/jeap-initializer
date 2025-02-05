package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolderRenamerContributorTest {

    @TempDir
    static Path tempDir;

    private FolderRenamerContributor contributor = new FolderRenamerContributor();

    @Test
    void contributeSingleModuleSrcMain() throws IOException {
        Path srcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/src/main/java/my/custom/package"));
        TestUtils.writeToFile(srcDirectory, "MyClass.java", "hello!");

        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("ch.admin.bit.jme"), TestUtils.templateWithBasePackage("my.custom.package"));

        assertTrue(exists(Path.of( tempDir.toString() + "/src/main/java/ch/admin/bit/jme/MyClass.java")));
        assertFalse(exists(Path.of( tempDir.toString() + "/src/test/java/ch/admin/bit/jme/MyClass.java")));
    }

    @Test
    void contributeMultiModuleSrcMain() throws IOException {
        Path srcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/my-module/src/main/java/my/custom/package"));
        TestUtils.writeToFile(srcDirectory, "MyClass.java", "hello!");

        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("ch.admin.bit.jme"), TestUtils.templateWithBasePackage("my.custom.package"));

        assertTrue(exists(Path.of( tempDir.toString() + "/my-module/src/main/java/ch/admin/bit/jme/MyClass.java")));
        assertFalse(exists(Path.of( tempDir.toString() + "/my-module/src/test/java/ch/admin/bit/jme/MyClass.java")));
    }

    @Test
    void contributeSingleModuleSrcTest() throws IOException {
        Path srcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/src/test/java/my/custom/package"));
        TestUtils.writeToFile(srcDirectory, "MyTestClass.java", "hello!");

        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("ch.admin.bit.jme"), TestUtils.templateWithBasePackage("my.custom.package"));

        assertTrue(exists(Path.of( tempDir.toString() + "/src/test/java/ch/admin/bit/jme/MyTestClass.java")));
        assertFalse(exists(Path.of( tempDir.toString() + "/src/main/java/ch/admin/bit/jme/MyTestClass.java")));
    }

    @Test
    void contributeMultiModuleSrcTest() throws IOException {
        Path srcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/my-module/src/test/java/my/custom/package"));
        TestUtils.writeToFile(srcDirectory, "MyTestClass.java", "hello!");

        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("ch.admin.bit.jme"), TestUtils.templateWithBasePackage("my.custom.package"));

        assertTrue(exists(Path.of( tempDir.toString() + "/my-module/src/test/java/ch/admin/bit/jme/MyTestClass.java")));
        assertFalse(exists(Path.of( tempDir.toString() + "/my-module/src/main/java/ch/admin/bit/jme/MyTestClass.java")));
    }

}