package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This contributor removes the initializer.yaml file
 */
@Slf4j
@Component
public class TemplateFileRemovalContributor implements ProjectContributor {

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) throws IOException {
        Path initializerYamlFile = projectRoot.resolve("initializer.yaml");
        if (Files.isRegularFile(initializerYamlFile)) {
            Files.delete(initializerYamlFile);
        }
    }
}
