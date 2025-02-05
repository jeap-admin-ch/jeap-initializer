package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParameterReplacementContributorTest {

    private ParameterReplacementContributor contributor;
    private ProjectRequest projectRequest;
    private ProjectTemplate projectTemplate;

    @TempDir
    Path projectRoot;

    @BeforeEach
    void setUp() {
        JeapInitializerProperties properties = new JeapInitializerProperties();
        properties.setSourceFilesPattern(Pattern.compile(".*\\.(java|json)"));
        contributor = new ParameterReplacementContributor(properties);
        projectRequest = new ProjectRequest();
        projectTemplate = new ProjectTemplate();
    }

    @Test
    void contributeReplacesParametersInSourceFiles() throws IOException {
        projectRequest.setTemplateParameters(Map.of("param1", "new-value"));
        Path filePath = projectRoot.resolve("Test.java");
        List<String> lines = List.of(
                "INITIALIZER PARAMETER param1 VALUE value1",
                "Some code with value1"
        );
        Files.write(filePath, lines);

        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of("Some code with new-value"), updatedLines);
    }

    @Test
    void contributeIgnoresFilesWithoutParameters() throws IOException {
        Path filePath = projectRoot.resolve("Test.java");
        List<String> lines = List.of("Some code without parameters");
        Files.write(filePath, lines);

        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(lines, updatedLines);
    }

    @Test
    void contributeHandlesEmptyFiles() throws IOException {
        Path filePath = projectRoot.resolve("Empty.java");
        List<String> lines = List.of();
        Files.write(filePath, lines);

        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(lines, updatedLines);
    }

    @Test
    void contributeHandlesMultipleParameters() throws IOException {
        projectRequest.setTemplateParameters(Map.of("param1", "new-value", "param2", "newValue2"));

        Path filePath = projectRoot.resolve("Test.java");
        List<String> lines = List.of(
                "INITIALIZER PARAMETER param1 VALUE value1",
                "INITIALIZER PARAMETER param2 VALUE value2",
                "Some code with value1 and value2"
        );
        Files.write(filePath, lines);

        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of("Some code with new-value and newValue2"), updatedLines);
    }

    @Test
    void contributeReplacesParametersInJsonSourceFiles() throws IOException {
        projectRequest.setTemplateParameters(Map.of("bucketName", "new-value"));
        Path filePath = projectRoot.resolve("test.json");
        String json = """
                "s3_buckets_read": [
                   "INITIALIZER PARAMETER bucketName VALUE replace-me",
                   "replace-me"
                 ],
                """;
        Files.writeString(filePath, json);

        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        assertThat(filePath).content().isEqualToIgnoringWhitespace("""
                "s3_buckets_read": [
                   "new-value"
                 ],
                """);
    }
}
