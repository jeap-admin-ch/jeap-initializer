package ch.admin.bit.jeap.initializer;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
public class TestUtils {

    public static void writeToFile(Path srcDirectory, String fileName, String content) throws IOException {
        Path file = Files.createFile(Path.of(srcDirectory.toString(), fileName));
        Files.writeString(file, content);
    }

    public static ProjectTemplate templateWithBasePackage(String basePackage) {
        ProjectTemplate projectTemplate = new ProjectTemplate();
        projectTemplate.setBasePackage(basePackage);
        return projectTemplate;
    }

    public static ProjectRequest requestWithBasePackage(String basePackage) {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setBasePackage(basePackage);
        return projectRequest;
    }

    public static void addTestFileToFolder(String fileName, Path path) throws IOException {
        final Path targetFile = path.resolve(fileName);

        Files.createDirectories(targetFile.getParent());

        try (InputStream inputStream = new ClassPathResource(fileName).getInputStream()) {
            Files.copy(inputStream, targetFile, REPLACE_EXISTING);
        }

        log.info("Add a file [{}] to the folder [{}] on the path [{}]", fileName, path, targetFile);
    }
}
