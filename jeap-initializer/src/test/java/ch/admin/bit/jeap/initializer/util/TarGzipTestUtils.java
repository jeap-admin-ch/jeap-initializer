package ch.admin.bit.jeap.initializer.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public class TarGzipTestUtils {

    public static void untarGzipFile(Path tarGzFilePath, Path targetDir) throws IOException {
        if (Files.notExists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(
                new GzipCompressorInputStream(Files.newInputStream(tarGzFilePath)))) {
            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                Path filePath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    if (filePath.getParent() != null && Files.notExists(filePath.getParent())) {
                        Files.createDirectories(filePath.getParent());
                    }
                    Files.copy(tarInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

                    // Restore POSIX file permissions
                    int mode = entry.getMode();
                    Set<PosixFilePermission> permissions = fromOctalMode(mode);
                    Files.setPosixFilePermissions(filePath, permissions);
                }
            }
        }
    }

    @SuppressWarnings("OctalInteger")
    private static Set<PosixFilePermission> fromOctalMode(int mode) {
        EnumSet<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);

        // Owner permissions (3 bits)
        if ((mode & 0400) != 0) permissions.add(PosixFilePermission.OWNER_READ);
        if ((mode & 0200) != 0) permissions.add(PosixFilePermission.OWNER_WRITE);
        if ((mode & 0100) != 0) permissions.add(PosixFilePermission.OWNER_EXECUTE);

        // Group permissions (3 bits)
        if ((mode & 0040) != 0) permissions.add(PosixFilePermission.GROUP_READ);
        if ((mode & 0020) != 0) permissions.add(PosixFilePermission.GROUP_WRITE);
        if ((mode & 0010) != 0) permissions.add(PosixFilePermission.GROUP_EXECUTE);

        // Others permissions (3 bits)
        if ((mode & 0004) != 0) permissions.add(PosixFilePermission.OTHERS_READ);
        if ((mode & 0002) != 0) permissions.add(PosixFilePermission.OTHERS_WRITE);
        if ((mode & 0001) != 0) permissions.add(PosixFilePermission.OTHERS_EXECUTE);

        return permissions;
    }
}
