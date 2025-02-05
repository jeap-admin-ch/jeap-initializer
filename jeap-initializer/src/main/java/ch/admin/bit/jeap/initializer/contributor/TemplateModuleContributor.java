package ch.admin.bit.jeap.initializer.contributor;

import ch.admin.bit.jeap.initializer.config.JeapInitializerProperties;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TemplateModuleContributor implements ProjectContributor {

    private final Pattern sourceFilesPattern;

    public TemplateModuleContributor(JeapInitializerProperties jeapInitializerProperties) {
        this.sourceFilesPattern = jeapInitializerProperties.getSourceFilesPattern();
    }

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        Set<String> removedModuleIds = new HashSet<>(template.getAllModuleIds());
        Set<String> selectedModuleIds = projectRequest.getSelectedModuleIds();
        removedModuleIds.removeAll(selectedModuleIds);

        log.info("Selected modules: {}, removing modules: {}", selectedModuleIds, removedModuleIds);

        removedModuleIds.forEach(moduleId -> removeModule(moduleId, projectRoot, projectRequest, template));
        selectedModuleIds.forEach(moduleId -> removeSelectedModuleMarkers(moduleId, projectRoot));
    }

    private void removeModule(String moduleId, Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) {
        // Remove code blocks that are specific to a module that is not included in the generated output
        CodeRemoverContributor codeRemoverContributor = new CodeRemoverContributor("MODULE " + moduleId, sourceFilesPattern);
        codeRemoverContributor.contribute(projectRoot, projectRequest, template);
        // Remove files that are specific to modules that are not included in the generated output
        FileUtils.deleteFilesContainingMarker(projectRoot, sourceFilesPattern, "MODULE-SPECIFIC FILE FOR MODULE " + moduleId);
    }

    private Pattern moduleSpecificLinePattern(String moduleId) {
        // Remove lines that contain module specific block start/end tags, or lines that mark files as module-specific
        return Pattern.compile(
                ".*(START MODULE %1$s|END MODULE %1$s|MODULE-SPECIFIC FILE FOR MODULE %1$s).*"
                        .formatted(moduleId));
    }

    private void removeSelectedModuleMarkers(String moduleId, Path projectRoot) {
        // Remove module-specific markers for selected modules
        Pattern removedLinePattern = moduleSpecificLinePattern(moduleId);
        FileUtils.deleteMatchingLines(projectRoot, sourceFilesPattern, removedLinePattern);
    }

    @Override
    public int getOrder() {
        // This contributor has a very high precedence to ensure that module-specific files are removed before other
        // contributors are applied.
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
