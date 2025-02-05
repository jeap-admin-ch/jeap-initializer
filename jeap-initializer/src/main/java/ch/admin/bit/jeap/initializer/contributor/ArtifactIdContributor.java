package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static ch.admin.bit.jeap.initializer.util.FileUtils.replaceInFiles;
import static io.micrometer.common.util.StringUtils.isBlank;

/**
 * This contributor modifies the module folders and poms to reflect the artifact id
 */
@Slf4j
@Component
public class ArtifactIdContributor implements ProjectContributor {

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        if (isBlank(template.getArtifactId()) || isBlank(projectRequest.getArtifactId())) {
            return;
        }

        renameModuleFolders(projectRoot, projectRequest, template);
        replaceInFiles("pom.xml", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("jenkinsfile(.*)", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());

        // Replacements in the UI-Module
        replaceInFiles("angular.json", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("environment(.*).ts", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("package(.*).json", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("proxy.conf.js", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("de.json", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("en.json", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("fr.json", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("it.json", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());

        Path runConfigurationsFolder = Path.of(projectRoot + "/.idea/runConfigurations");
        if (Files.exists(runConfigurationsFolder)) {
            replaceInFiles("(.*).xml", runConfigurationsFolder, template.getArtifactId(), projectRequest.getArtifactId());
        }
    }

    private static void renameModuleFolders(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        try (Stream<Path> stream = Files.walk(projectRoot)) {
            List<Path> modules = stream.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().contains(template.getArtifactId()))
                    .toList();

            modules.forEach(module -> {
                Path targetPath = Path.of(module.toString().replace(template.getArtifactId(), projectRequest.getArtifactId()));
                try {
                    Files.createDirectories(targetPath);
                    Files.move(module, targetPath, StandardCopyOption.ATOMIC_MOVE);
                    log.debug("Renamed module {} to {}", module, targetPath);
                } catch (IOException e) {
                    throw FileProcessingException.ioException(e);
                }
            });
        } catch (IOException ioException) {
            throw FileProcessingException.ioException(ioException);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
