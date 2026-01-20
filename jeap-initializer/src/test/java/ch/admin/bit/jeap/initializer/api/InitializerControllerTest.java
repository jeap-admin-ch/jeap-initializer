package ch.admin.bit.jeap.initializer.api;

import ch.admin.bit.jeap.initializer.api.model.ProjectTemplateDTO;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.util.TarGzipTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InitializerControllerTest {

    public static final ParameterizedTypeReference<List<ProjectTemplateDTO>> LIST_PROJECTTEMPLATEDTO_TYPE_REF = new ParameterizedTypeReference<>() {
    };

    @LocalServerPort
    private int randomServerPort;

    private final RestClient restClient = RestClient.create();

    @Test
    void generateProjectWithDefaults(@TempDir Path extractedDirectory) throws IOException {
        ProjectRequest projectRequest = getProjectRequest();
        ResponseEntity<byte[]> response = restClient.post()
                .uri("http://localhost:" + randomServerPort + "/api/generate")
                .body(projectRequest)
                .retrieve()
                .toEntity(byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().containsKey("content-type")).isTrue();
        assertThat(response.getHeaders().get("Content-Type").get(0)).isEqualTo("application/octet-stream");
        assertThat(response.getHeaders().get("Content-Disposition").get(0)).containsPattern("attachment; filename=\"jEAP Project(\\d{4}-\\d{2}-\\d{2}-\\d{6}).tar.gz\"");

        extractGzipResponse(response, extractedDirectory);

        assertThat(Files.exists(Path.of(extractedDirectory + "/README.md"))).isTrue();
    }

    @Test
    void generateProjectCustomApplicationName(@TempDir Path extractedDirectory) throws IOException {
        ProjectRequest projectRequest = getProjectRequest();
        projectRequest.setApplicationName("myApp");
        ResponseEntity<byte[]> response = restClient.post()
                .uri("http://localhost:" + randomServerPort + "/api/generate")
                .body(projectRequest)
                .retrieve()
                .toEntity(byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().containsKey("content-type")).isTrue();
        assertThat(response.getHeaders().get("Content-Type").get(0)).isEqualTo("application/octet-stream");
        assertThat(response.getHeaders().get("Content-Disposition").get(0)).containsPattern("attachment; filename=\"myApp(\\d{4}-\\d{2}-\\d{2}-\\d{6}).tar.gz\"");

        extractGzipResponse(response, extractedDirectory);

        assertThat(Files.exists(Path.of(extractedDirectory + "/README.md"))).isTrue();
    }

    @Test
    void generateProjectWithUnknownTemplate_shouldFail() {
        ProjectRequest projectRequest = getProjectRequest();
        projectRequest.setTemplate("wrong");

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restClient.post()
                    .uri("http://localhost:" + randomServerPort + "/api/generate")
                    .body(projectRequest)
                    .retrieve()
                    .toEntity(byte[].class);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).contains("No template found with key wrong");
    }

    @Test
    void generateProjectWithoutRequiredTemplateParameter_shouldFail() {
        ProjectRequest projectRequest = new ProjectRequest();

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restClient.post()
                    .uri("http://localhost:" + randomServerPort + "/api/generate")
                    .body(projectRequest)
                    .retrieve()
                    .toEntity(byte[].class);
        });
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).contains("Required template parameter 'templateParameter1' is missing");
    }

    @Test
    void getTemplates() {
        ResponseEntity<List<ProjectTemplateDTO>> getTemplatesResponse = restClient.get()
                .uri("http://localhost:" + randomServerPort + "/api/templates")
                .retrieve()
                .toEntity(LIST_PROJECTTEMPLATEDTO_TYPE_REF);
        List<ProjectTemplateDTO> templates = getTemplatesResponse.getBody();
        assertThat(templates.size()).isEqualTo(2);
        ProjectTemplateDTO template = templates.get(0);
        assertThat(template.key()).isEqualTo("jeap-scs");
        assertThat(template.name()).isEqualTo("a My template");
        assertThat(template.description()).isEqualTo("Some description");
        assertThat(template.templateParameters().size()).isEqualTo(2);
        assertThat(template.templateParameters().getFirst().getId()).isEqualTo("templateParameter1");
        assertThat(template.templateParameters().getFirst().getName()).isEqualTo("Template Parameter 1");
        assertThat(template.templateParameters().getFirst().getDescription()).isEqualTo("Description of templateParameter1");
        assertThat(template.templateParameters().getLast().getId()).isEqualTo("templateParameter2");
        assertThat(template.templateParameters().getLast().getName()).isEqualTo("Template Parameter 2");
        assertThat(template.templateParameters().getLast().getDescription()).isEqualTo("Description of templateParameter2");

        template = templates.get(1);
        assertThat(template.key()).isEqualTo("gitops-template");
        assertThat(template.name()).isEqualTo("b My template");
        assertThat(template.description()).isEqualTo("Some description");
        assertThat(template.templateParameters().size()).isEqualTo(2);
        assertThat(template.templateParameters().getFirst().getId()).isEqualTo("templateParameter1");
        assertThat(template.templateParameters().getFirst().getName()).isEqualTo("Template Parameter 1");
        assertThat(template.templateParameters().getFirst().getDescription()).isEqualTo("Description of templateParameter1");
        assertThat(template.templateParameters().getLast().getId()).isEqualTo("templateParameter2");
        assertThat(template.templateParameters().getLast().getName()).isEqualTo("Template Parameter 2");
        assertThat(template.templateParameters().getLast().getDescription()).isEqualTo("Description of templateParameter2");
    }

    private static void extractGzipResponse(ResponseEntity<byte[]> response, Path extractedDirectory) throws IOException {
        Path tarGzipFile = null;
        try {
            tarGzipFile = Files.createTempFile("tmp", ".tar.gz");
            Files.write(tarGzipFile, requireNonNull(response.getBody()));
            TarGzipTestUtils.untarGzipFile(tarGzipFile, extractedDirectory);
        } finally {
            if (tarGzipFile != null) {
                Files.deleteIfExists(tarGzipFile);
            }
        }
    }

    private static ProjectRequest getProjectRequest() {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setTemplate("jeap-scs"); // Set a valid template key
        Map<String, String> templateParameters = new HashMap<>();
        templateParameters.put("templateParameter1", "value1");
        templateParameters.put("templateParameter2", "value2");
        projectRequest.setTemplateParameters(templateParameters);
        return projectRequest;
    }

}
