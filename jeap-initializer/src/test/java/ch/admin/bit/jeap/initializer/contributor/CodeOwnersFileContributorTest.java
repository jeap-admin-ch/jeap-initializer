package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.admin.bit.jeap.initializer.TestUtils.addTestFileToFolder;
import static java.nio.file.Files.createDirectories;
import static org.junit.jupiter.api.Assertions.*;

class CodeOwnersFileContributorTest {

    private static final String CODEOWNERS_FILE = "CODEOWNERS";
    private static final String TEST_RESOURCES_PATH = "bit-jme-app-service/src/main/resources";
    private static final String CODE_OWNERS_PATH = TEST_RESOURCES_PATH + "/" + CODEOWNERS_FILE;
    private static final String CODE_OWNERS_ID = "codeOwners";
    private static final String CODE_OWNERS_NAME = "Application Code Owners";
    private static final String CODE_OWNERS_DESCRIPTION = "People or teams responsible for the code in a repo, ex. @NIVEL-BCM/NIVEL-BCM-bazg-nivel";
    private static final String CODE_OWNERS_TEST_VALUE = "@NIVEL-BCM/NIVEL-BCM-bazg-nivel";

    private static final String SENTINEL_ORIGINAL_CONTENT = "# sentinel: keep me if no codeOwners is provided\n* @someone-else\n";

    private final CodeOwnersFilesContributor contributor = new CodeOwnersFilesContributor();

    @TempDir
    private Path projectRootPath;

    @Test
    void codeOwnersFileAdapted() throws IOException {
        setFile();

        final ProjectTemplate template = getProjectTemplate();
        final ProjectRequest request = getProjectRequest();

        contributor.contribute(projectRootPath, request, template);

        // Verification
        final String codeOwnerFile = Files.readString(Path.of(projectRootPath + "/" + CODE_OWNERS_PATH));
        assertTrue(codeOwnerFile.contains(CODE_OWNERS_TEST_VALUE));
    }

    @Test
    void contribute_doesNothing_whenProjectRequestIsNull() throws IOException {
        setFileWithSentinelContent();

        contributor.contribute(projectRootPath, null, getProjectTemplate());

        final String codeOwnerFile = Files.readString(Path.of(projectRootPath + "/" + CODE_OWNERS_PATH));
        assertEquals(SENTINEL_ORIGINAL_CONTENT, codeOwnerFile);
    }

    @Test
    void contribute_doesNothing_whenCodeOwnersParameterIsMissing() throws IOException {
        setFileWithSentinelContent();

        final ProjectRequest request = new ProjectRequest();
        request.setTemplateParameters(Map.of()); // no "codeOwners"

        contributor.contribute(projectRootPath, request, getProjectTemplate());

        final String codeOwnerFile = Files.readString(Path.of(projectRootPath + "/" + CODE_OWNERS_PATH));
        assertEquals(SENTINEL_ORIGINAL_CONTENT, codeOwnerFile);
    }

    @Test
    void contribute_doesNothing_whenCodeOwnersParameterIsEmptyString() throws IOException {
        setFileWithSentinelContent();

        final ProjectRequest request = new ProjectRequest();
        request.setTemplateParameters(Map.of(CODE_OWNERS_ID, "")); // empty

        contributor.contribute(projectRootPath, request, getProjectTemplate());

        final String codeOwnerFile = Files.readString(Path.of(projectRootPath + "/" + CODE_OWNERS_PATH));
        assertEquals(SENTINEL_ORIGINAL_CONTENT, codeOwnerFile);
    }

    @Test
    void contribute_overwritesAllMatchingFiles_whenMultipleCodeownersFilesExist() throws IOException {
        // Arrange: create two CODEOWNERS files in different locations
        createDirectories(projectRootPath.resolve(TEST_RESOURCES_PATH));
        addTestFileToFolder(CODE_OWNERS_PATH, projectRootPath);

        final Path secondCodeOwners = projectRootPath.resolve("some/other/module/" + CODEOWNERS_FILE);
        Files.createDirectories(secondCodeOwners.getParent());
        Files.writeString(secondCodeOwners, SENTINEL_ORIGINAL_CONTENT);

        // Act
        contributor.contribute(projectRootPath, getProjectRequest(), getProjectTemplate());

        // Assert: both are overwritten with the provided value (full content overwrite)
        final String first = Files.readString(projectRootPath.resolve(CODE_OWNERS_PATH));
        final String second = Files.readString(secondCodeOwners);

        assertEquals(CODE_OWNERS_TEST_VALUE, first);
        assertEquals(CODE_OWNERS_TEST_VALUE, second);
    }

    @Test
    void contribute_matchesFileNameCaseInsensitively() throws IOException {
        // Arrange: create a differently-cased "CodeOwners" file
        final Path mixedCase = projectRootPath.resolve(TEST_RESOURCES_PATH + "/CodeOwners");
        Files.createDirectories(mixedCase.getParent());
        Files.writeString(mixedCase, SENTINEL_ORIGINAL_CONTENT);

        // Act
        contributor.contribute(projectRootPath, getProjectRequest(), getProjectTemplate());

        // Assert
        final String updated = Files.readString(mixedCase);
        assertEquals(CODE_OWNERS_TEST_VALUE, updated);
    }

    @Test
    void contribute_createsFileAtProjectRoot_whenNoCodeownersFileExists() throws IOException {
        // Arrange: do NOT create any CODEOWNERS file in the tree

        // Act
        contributor.contribute(projectRootPath, getProjectRequest(), getProjectTemplate());

        // Assert: FileUtils.overwriteFileContent creates projectRoot/CODEOWNERS if none existed
        final Path createdAtRoot = projectRootPath.resolve(CODEOWNERS_FILE);
        assertTrue(Files.exists(createdAtRoot));

        final String content = Files.readString(createdAtRoot);
        assertEquals(CODE_OWNERS_TEST_VALUE, content);

        // And: it should not have created the resource-path CODEOWNERS implicitly
        assertFalse(Files.exists(projectRootPath.resolve(CODE_OWNERS_PATH)));
    }

    private static ProjectRequest getProjectRequest() {
        final ProjectRequest request = new ProjectRequest();
        final Map<String, String> templateParameters = new HashMap<>();
        templateParameters.put(CODE_OWNERS_ID, CODE_OWNERS_TEST_VALUE);
        request.setTemplateParameters(templateParameters);
        return request;
    }

    private static ProjectTemplate getProjectTemplate() {
        final ProjectTemplate template = new ProjectTemplate();
        template.setTemplateParameters(
                List.of(new TemplateParameter(CODE_OWNERS_ID, CODE_OWNERS_NAME, CODE_OWNERS_DESCRIPTION))
        );
        return template;
    }

    private void setFile() throws IOException {
        createDirectories(Path.of(projectRootPath + "/" + TEST_RESOURCES_PATH));
        addTestFileToFolder(CODE_OWNERS_PATH, projectRootPath);
    }

    private void setFileWithSentinelContent() throws IOException {
        setFile();
        Files.writeString(projectRootPath.resolve(CODE_OWNERS_PATH), SENTINEL_ORIGINAL_CONTENT);
    }
}