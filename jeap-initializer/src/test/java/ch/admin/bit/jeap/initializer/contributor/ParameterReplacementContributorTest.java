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
import org.springframework.core.Ordered;

class ParameterReplacementContributorTest {

    private static final String CODEOWNERS_FILE = "CODEOWNERS";
    private static final String CODE_OWNERS_ID = "codeOwners";
    private static final String NEW_TEAM = "@BAZG-System/bazg-system-bazg-saluver";
    private static final String OLD_TEAM = "@BIT-JME/bit-jme-bazg-margun";
    private static final String BAZG_FLIX = "@BAZG-System/bazg-system-bazg-flix";
    private static final String BAZG_TAVERNA = "@BAZG-System/bazg-system-bazg-taverna";
    private static final String BAZG_GRISCHA = "@BAZG-System/bazg-system-bazg-grischa";
    private static final Pattern sourceFilesPattern = Pattern.compile("CODEOWNERS|Dockerfile|Jenkinsfile.*|(.+\\.(md|html|css|java|xml|yaml|yml|properties|json|conf|ts))", Pattern.CASE_INSENSITIVE);

    private ParameterReplacementContributor contributor;
    private ProjectRequest projectRequest;
    private ProjectTemplate projectTemplate;

    @TempDir
    Path projectRoot;

    @BeforeEach
    void setUp() {
        JeapInitializerProperties properties = new JeapInitializerProperties();
        properties.setSourceFilesPattern(sourceFilesPattern);
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
    void contributeReplacesCodeOwnersParameterInCodeOwnerFile() throws IOException {
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, NEW_TEAM));

        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);

        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(NEW_TEAM), updatedLines);
    }

    @Test
    void contributeReplacesCodeOwnersParameterWithFlix() throws IOException {
        String newTeam = BAZG_FLIX;
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, newTeam));
        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);
        contributor.contribute(projectRoot, projectRequest, projectTemplate);
        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(newTeam), updatedLines);
    }

    @Test
    void contributeReplacesCodeOwnersParameterWithTaverna() throws IOException {
        String newTeam = BAZG_TAVERNA;
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, newTeam));
        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);
        contributor.contribute(projectRoot, projectRequest, projectTemplate);
        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(newTeam), updatedLines);
    }

    @Test
    void contributeReplacesCodeOwnersParameterWithGrischa() throws IOException {
        String newTeam = BAZG_GRISCHA;
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, newTeam));
        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);
        contributor.contribute(projectRoot, projectRequest, projectTemplate);
        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(newTeam), updatedLines);
    }

    @Test
    void contributeReplacesCodeOwnersParameterWithMultipleOwners() throws IOException {
        String newTeam = BAZG_FLIX + ", " + BAZG_TAVERNA;
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, newTeam));
        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);
        contributor.contribute(projectRoot, projectRequest, projectTemplate);
        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(newTeam), updatedLines);
    }

    @Test
    void contributeReplacesCodeOwnersParameterWithSimilarNames() throws IOException {
        String newTeam = "@BAZG-System/bazg-system-bazg-taverna; @BAZG-System/bazg-system-bazg-taverna-extra";
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, newTeam));
        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);
        contributor.contribute(projectRoot, projectRequest, projectTemplate);
        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(newTeam), updatedLines);
    }

    @Test
    void contributeReplacesCodeOwnersParameterWithNames() throws IOException {
        String newTeam = "@BAZG-System/bazg-system-bazg-melnetta @BAZG-System/bazg-system-bazg-flix";
        projectRequest.setTemplateParameters(Map.of(CODE_OWNERS_ID, newTeam));
        Path filePath = projectRoot.resolve(CODEOWNERS_FILE);
        List<String> lines = List.of(
                "INITIALIZER PARAMETER " + CODE_OWNERS_ID + " VALUE " + OLD_TEAM,
                OLD_TEAM
        );
        Files.write(filePath, lines);
        contributor.contribute(projectRoot, projectRequest, projectTemplate);
        List<String> updatedLines = Files.readAllLines(filePath);
        assertEquals(List.of(newTeam), updatedLines);
    }

    @Test
    void contribute() throws IOException {
        // Arrange: one matching file and one non-matching file
        projectRequest.setTemplateParameters(Map.of("paramX", "new"));

        Path matching = projectRoot.resolve("config.properties");
        Files.write(matching, List.of(
                "# some header",
                "INITIALIZER PARAMETER paramX VALUE old",
                "value=old"
        ));

        Path nonMatching = projectRoot.resolve("ignore.bin");
        List<String> originalNonMatching = List.of(
                "INITIALIZER PARAMETER paramX VALUE old",
                "binary old"
        );
        Files.write(nonMatching, originalNonMatching);

        // Act
        contributor.contribute(projectRoot, projectRequest, projectTemplate);

        // Assert: matching file is processed, marker removed and value replaced
        assertThat(Files.readAllLines(matching))
                .containsExactly(
                        "# some header",
                        "value=new"
                );

        // Assert: non-matching file remains untouched
        assertThat(Files.readAllLines(nonMatching)).containsExactlyElementsOf(originalNonMatching);
    }

    @Test
    void getOrder() {
        assertThat(contributor.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 10);
    }
}
