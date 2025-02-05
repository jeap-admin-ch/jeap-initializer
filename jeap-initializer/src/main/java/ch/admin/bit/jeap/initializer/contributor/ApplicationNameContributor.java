package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static ch.admin.bit.jeap.initializer.util.FileUtils.replaceInFiles;
import static io.micrometer.common.util.StringUtils.isBlank;

/**
 * This contributor modifies the application name
 */
@Slf4j
@Component
public class ApplicationNameContributor implements ProjectContributor {

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        if (isBlank(template.getName()) || isBlank(projectRequest.getApplicationName())) {
            return;
        }

        replaceInFiles("README\\.md", projectRoot, template.getName(), projectRequest.getApplicationName());
        replaceInFiles("index\\.html", projectRoot, template.getName(), projectRequest.getApplicationName());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
