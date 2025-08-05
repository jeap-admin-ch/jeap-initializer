package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.micrometer.common.util.StringUtils.isBlank;

/**
 * This contributor modifies the source folders to use the base package
 */
@Slf4j
@Component
public class FolderRenamerContributor implements ProjectContributor {
    
    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        if (isBlank(template.getBasePackage()) || isBlank(projectRequest.getBasePackage())) {
            return;
        }

        String sourcePackageFolder = template.getBasePackage().replace(".", File.separator);
        String targetPackageFolder = projectRequest.getBasePackage().replace(".", File.separator);

        try (Stream<Path> stream = Files.walk(projectRoot)) {
            List<Path> srcFolders = stream.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase("src"))
                    .toList();
            
            srcFolders.forEach(srcFolder -> {
                moveMavenPath("/main/java/", srcFolder, sourcePackageFolder, targetPackageFolder);
                moveMavenPath("/test/java/", srcFolder, sourcePackageFolder, targetPackageFolder);
            });
        } catch (IOException ioException) {
            throw FileProcessingException.ioException(ioException);
        }

    }

    private static void moveMavenPath(String mavenPath, Path srcFolder, String sourcePackageFolder, String targetPackageFolder) {
        Path sourcePath = Path.of(srcFolder.toString() + mavenPath + sourcePackageFolder);
        Path targetPath = Path.of(srcFolder.toString() + mavenPath + targetPackageFolder);

        if (!Files.exists(sourcePath)) {
            return;
        }

        // Normalize to absolute paths to make startsWith behave predictably
        Path src = sourcePath.toAbsolutePath().normalize();
        Path tgt = targetPath.toAbsolutePath().normalize();

        if (src.equals(tgt)) {
            // Nothing to do
            return;
        }

        try {
            if (!tgt.startsWith(src)) {
                Files.createDirectories(tgt.getParent());
                moveAtomicWithFallback(src, tgt);
                log.debug("Moved source path {} to {}", src, tgt);
            } else {
                moveToSubDir(src, tgt);
                log.debug("Moved source path {} to subpath {}", src, tgt);
            }
        } catch (IOException e) {
            throw FileProcessingException.ioException(e);
        }
    }

    private static void moveToSubDir(Path src, Path tgt) throws IOException {
        Path sourceParent = src.getParent();
        if (sourceParent == null) {
            throw new IllegalArgumentException("Source directory must have a parent: " + src);
        }

        // rename source directory to a temp sibling
        String sourceDirname = src.getFileName().toString();
        Path tmp = sourceParent.resolve(sourceDirname + ".tmp-" + UUID.randomUUID());
        moveAtomicWithFallback(src, tmp);

        // move tmp into the desired subdir of the source directory
        Files.createDirectories(tgt.getParent());
        moveAtomicWithFallback(tmp, tgt);
    }

    private static void moveAtomicWithFallback(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
