package ch.admin.bit.jeap.initializer.api;

import ch.admin.bit.jeap.initializer.api.model.ProjectTemplateDTO;
import ch.admin.bit.jeap.initializer.generator.ProjectGenerator;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.template.TemplateService;
import ch.admin.bit.jeap.initializer.util.TarGzipUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Slf4j
public class InitializerController {

    private final ProjectGenerator projectGenerator;
    private final TemplateService templateService;
    private final CacheManager cacheManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    @Operation(
            summary = "Generates a project based on a template",
            description = "Generates a project based on a template")
    @ApiResponse(responseCode = "200", description = "The project has been successfully generated")
    @ApiResponse(responseCode = "404", description = "The given template has not been found")
    @PostMapping("/generate")
    public ResponseEntity<InputStreamResource> generate(@RequestBody ProjectRequest projectRequest) throws IOException {
        log.trace("Generating project with parameters: {}", projectRequest);

        Path projectFolder = Files.createTempDirectory("jeap-initializer");
        try {
            projectGenerator.generate(projectRequest, projectFolder);

            Path tarGzipFile = Files.createTempFile("jeapProject", ".tar.gz");
            TarGzipUtils.tarGzipDirectory(projectFolder, tarGzipFile);

            InputStreamResource resource = new InputStreamResource(Files.newInputStream(tarGzipFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + generateFileName(projectRequest) + "\""); // Forces file download
            headers.add(HttpHeaders.CONTENT_TYPE, "application/gzip");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } finally {
            FileSystemUtils.deleteRecursively(projectFolder);
        }
    }

    @GetMapping("/templates")
    public List<ProjectTemplateDTO> getProjectTemplates() {
        return templateService.getProjectTemplates().stream()
                .map(ProjectTemplateDTO::from)
                .toList();
    }

    @PostMapping("/cache/reset")
    public void resetTemplateCache() {
        cacheManager.getCacheNames().forEach(name ->
                cacheManager.getCache(name).clear());
    }

    private String generateFileName(ProjectRequest projectRequest) {
        return projectRequest.getApplicationName() + dateFormat.format(new Date()) + ".tar.gz";
    }
}
