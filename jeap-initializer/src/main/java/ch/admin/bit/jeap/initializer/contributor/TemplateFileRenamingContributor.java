package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * This contributor copies all files that end with ".initializer-template" to their original names (same name without ".initializer-template" ending).
 * This is used for files where we can't work with comments for replacement, e.g. json files.
 */
@Slf4j
@Component
public class TemplateFileRenamingContributor implements ProjectContributor {

    private static final String REPLACEMENT_ENDING = ".initializer-template";

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        try (Stream<Path> pathStream = Files.walk(projectRoot)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(REPLACEMENT_ENDING))
                    .forEach(this::renameFile);
        } catch (IOException e) {
            throw FileProcessingException.ioException("Failed to process files in project root: " + projectRoot, e);
        }
    }

    private void renameFile(Path originalPath) {
        try {
            String originalFileName = originalPath.getFileName().toString();
            String newFileName = originalFileName.substring(0, originalFileName.length() - REPLACEMENT_ENDING.length());

            Path newPath = originalPath.getParent().resolve(newFileName);

            // If target file already exists, delete it first
            if (Files.exists(newPath)) {
                Files.delete(newPath);
            }

            Files.move(originalPath, newPath);

        } catch (IOException e) {
            throw FileProcessingException.ioException("Failed to rename file: " + originalPath, e);
        }
    }
}
