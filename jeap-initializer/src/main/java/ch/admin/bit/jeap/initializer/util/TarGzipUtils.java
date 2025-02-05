package ch.admin.bit.jeap.initializer.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.stream.Stream;

import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.LONGFILE_POSIX;

public class TarGzipUtils {

    public static void tarGzipDirectory(Path sourceDirPath, Path outputFile) throws IOException {
        try (TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(
                new GzipCompressorOutputStream(Files.newOutputStream(outputFile)))) {
            tarOutputStream.setLongFileMode(LONGFILE_POSIX); // allow file paths longer than 100 bytes
            try (Stream<Path> stream = Files.walk(sourceDirPath)) {
                stream.filter(path -> !Files.isDirectory(path))
                        .forEach(path -> addEntry(sourceDirPath, path, tarOutputStream));
            }
        }
    }

    private static void addEntry(Path sourceDirPath, Path path, TarArchiveOutputStream tarOutputStream) {
        try {
            TarArchiveEntry tarEntry = new TarArchiveEntry(path.toFile(), sourceDirPath.relativize(path).toString());

            // Preserve POSIX file permissions
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
            int mode = toOctalMode(permissions);
            tarEntry.setMode(mode);

            tarOutputStream.putArchiveEntry(tarEntry);
            Files.copy(path, tarOutputStream);
            tarOutputStream.closeArchiveEntry();
        } catch (IOException e) {
            throw FileProcessingException.ioException("Error while creating tar.gz file: " + path, e);
        }
    }

    @SuppressWarnings("OctalInteger")
    private static int toOctalMode(Set<PosixFilePermission> permissions) {
        int mode = 0;

        // Owner permissions (3 bits)
        if (permissions.contains(PosixFilePermission.OWNER_READ)) mode |= 0400;
        if (permissions.contains(PosixFilePermission.OWNER_WRITE)) mode |= 0200;
        if (permissions.contains(PosixFilePermission.OWNER_EXECUTE)) mode |= 0100;

        // Group permissions (3 bits)
        if (permissions.contains(PosixFilePermission.GROUP_READ)) mode |= 0040;
        if (permissions.contains(PosixFilePermission.GROUP_WRITE)) mode |= 0020;
        if (permissions.contains(PosixFilePermission.GROUP_EXECUTE)) mode |= 0010;

        // Others permissions (3 bits)
        if (permissions.contains(PosixFilePermission.OTHERS_READ)) mode |= 0004;
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE)) mode |= 0002;
        if (permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) mode |= 0001;

        return mode;
    }
}
