package ch.admin.bit.jeap.initializer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static ch.admin.bit.jeap.initializer.TestUtils.writeToFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TarGzipUtilsTest {

    @TempDir
    private Path tempDir;
    @TempDir
    private Path extractedDir;

    @Test
    void tarGzipDirectory() throws IOException {
        writeToFile(tempDir, "file.txt", "hello from root folder!");
        Path srcDirectory = Files.createDirectories(tempDir.resolve("folder"));
        writeToFile(srcDirectory, "file-in-folder.txt", "hello from subfolder!");
        Path testFile = Files.createFile(tempDir.resolve("executable.sh"));
        Files.setPosixFilePermissions(testFile, PosixFilePermissions.fromString("rwxr-xr-x"));
        String longFilename = "1234567890".repeat(11);
        writeToFile(tempDir, longFilename, "long file content");

        Path tarGzipFile = Files.createTempFile("testTarGzipDirectory", ".tar.gz");
        tarGzipFile.toFile().deleteOnExit();
        TarGzipUtils.tarGzipDirectory(tempDir, tarGzipFile);

        TarGzipTestUtils.untarGzipFile(tarGzipFile, extractedDir);
        assertEquals("hello from root folder!", Files.readString(extractedDir.resolve("file.txt")));
        assertEquals("hello from subfolder!", Files.readString(extractedDir.resolve("folder/file-in-folder.txt")));
        assertEquals("long file content", Files.readString(extractedDir.resolve(longFilename)));
        assertEquals("rwxr-xr-x", PosixFilePermissions.toString(Files.getPosixFilePermissions(extractedDir.resolve("executable.sh"))));
    }
}
