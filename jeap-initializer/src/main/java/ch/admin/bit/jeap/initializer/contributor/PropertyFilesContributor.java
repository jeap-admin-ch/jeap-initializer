package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

import static ch.admin.bit.jeap.initializer.util.FileUtils.replaceInFiles;
import static io.micrometer.common.util.StringUtils.isBlank;

/**
 * This contributor modifies the System Name, the ArtifactId and the Context Path in the following
 * Files: application(.*).yml, application(.*).yaml, application(.*).properties, environment(.*).ts
 *
 */
@Slf4j
@Component
public class PropertyFilesContributor implements ProjectContributor {

    private static final String TEMPLATE_CONTEXT_PATH  = "jme-jeap-nivel-quadrel-project-template";

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {


        if (isBlank(template.getSystemName()) || isBlank(projectRequest.getSystemName()) ||
            isBlank(template.getArtifactId()) || isBlank(projectRequest.getArtifactId())) {
            return;
        }

        // Replace ArtifactId
        replaceInFiles("application(.*).yml", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("application(.*).yaml", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());
        replaceInFiles("application(.*).properties", projectRoot, template.getArtifactId(), projectRequest.getArtifactId());

        // Replace the Context Path
        String applicationContextPath = getContextPathFromProjectRequest(projectRequest);
        if (applicationContextPath != null) {
            replaceInFiles("application(.*).yml", projectRoot, TEMPLATE_CONTEXT_PATH, applicationContextPath);
            replaceInFiles("application(.*).yaml", projectRoot, TEMPLATE_CONTEXT_PATH, applicationContextPath);
            replaceInFiles("application(.*).properties", projectRoot, TEMPLATE_CONTEXT_PATH, applicationContextPath);
        }

        // Replace System Name
        replaceInFiles("application(.*).yml", projectRoot, template.getSystemName(), projectRequest.getSystemName());
        replaceInFiles("application(.*).yaml", projectRoot, template.getSystemName(), projectRequest.getSystemName());
        replaceInFiles("application(.*).properties", projectRoot, template.getSystemName(), projectRequest.getSystemName());
        replaceInFiles("environment(.*).ts", projectRoot, template.getSystemName(), projectRequest.getSystemName());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    private String getContextPathFromProjectRequest(ProjectRequest projectRequest) {
        if (projectRequest == null) return null;
        Map<String, String> templateParameters = projectRequest.getTemplateParameters();
        return templateParameters != null ? templateParameters.get("applicationContextPath") : null;
    }
}
