package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

import static ch.admin.bit.jeap.initializer.util.FileUtils.replaceInFiles;
import static io.micrometer.common.util.StringUtils.isBlank;

/**
 * This ProjectContributor modifies all package names inside Java files to use the base package
 */
@Component
public class JavaPackageRenamerContributor implements ProjectContributor {

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        if (isBlank(template.getBasePackage()) || isBlank(projectRequest.getBasePackage())) {
            return;
        }

        replaceInFiles("(.*).java", projectRoot, template.getBasePackage(), projectRequest.getBasePackage());

        Path runConfigurationsFolder = Path.of(projectRoot + "/.idea/runConfigurations");
        if (Files.exists(runConfigurationsFolder)) {
            replaceInFiles("(.*).xml", runConfigurationsFolder, template.getBasePackage(), projectRequest.getBasePackage());
        }
    }

}
