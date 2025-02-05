package ch.admin.bit.jeap.initializer;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        Path targetFile = path.resolve(fileName);
        Files.copy(new ClassPathResource("./" + fileName).getInputStream(), targetFile);
    }
}
