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

    @Test
    void contributeToSubPackageTarget() throws IOException {
        Path mainSrcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/src/main/java/ch/admin/bit/jme"));
        Path mainSrcSubDirectory = Files.createDirectories(mainSrcDirectory.resolve("foo"));
        TestUtils.writeToFile(mainSrcDirectory, "MyClassA.java", "Hello A!");
        TestUtils.writeToFile(mainSrcSubDirectory, "MyClassB.java", "Hello B!");

        // The request package is a subpackage of the template package
        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("ch.admin.bit.jme.my.custom.package"), TestUtils.templateWithBasePackage("ch.admin.bit.jme"));

        assertTrue(exists(mainSrcDirectory.resolve("my/custom/package/MyClassA.java")));
        assertTrue(exists(mainSrcDirectory.resolve("my/custom/package/foo/MyClassB.java")));
        Path testSrcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/src/test/java/ch/admin/bit/jme"));
        assertFalse(exists(testSrcDirectory.resolve("my/custom/package/MyClassA.java")));
        assertFalse(exists(testSrcDirectory.resolve("my/custom/package/foo/MyClassB.java")));
    }

    @Test
    void contributeToParentPackageTarget() throws IOException {
        Path mainSrcDirectory = Files.createDirectories(Path.of(tempDir.toString() + "/src/main/java/ch/admin/bit/jme"));
        Path mainSrcSubDirectory = Files.createDirectories(mainSrcDirectory.resolve("foo"));
        TestUtils.writeToFile(mainSrcDirectory, "MyClassA.java", "Hello A!");
        TestUtils.writeToFile(mainSrcSubDirectory, "MyClassB.java", "Hello B!");

        // The request package is a parent package of the template package
        contributor.contribute(tempDir, TestUtils.requestWithBasePackage("ch.admin.my.custom.package"), TestUtils.templateWithBasePackage("ch.admin.bit.jme"));

        assertTrue(exists(Path.of( tempDir.toString() + "/src/main/java/ch/admin/my/custom/package/MyClassA.java")));
        assertTrue(exists(Path.of( tempDir.toString() + "/src/main/java/ch/admin/my/custom/package/foo/MyClassB.java")));
        assertFalse(exists(Path.of( tempDir.toString() + "/src/test/java/ch/admin/my/custom/package/MyClassA.java")));
        assertFalse(exists(Path.of( tempDir.toString() + "/src/test/java/ch/admin/my/custom/package/foo/MyClassB.java")));
    }

}
