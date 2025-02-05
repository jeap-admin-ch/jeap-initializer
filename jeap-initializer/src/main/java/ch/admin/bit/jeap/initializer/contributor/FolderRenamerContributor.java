package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
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

        if (Files.exists(sourcePath)) {
            try {
                Files.createDirectories(targetPath);
                Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
                log.debug("Moved source path {} to {}", sourcePath, targetPath);
            } catch (IOException e) {
                throw FileProcessingException.ioException(e);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }
}
