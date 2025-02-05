package ch.admin.bit.jeap.initializer.template;

import ch.admin.bit.jeap.initializer.config.TemplateNotFoundException;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateModule;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Component
public class TemplateService {

    private final TemplateRepository templateRepository;

    public ProjectTemplate getTemplate(String templateKey) {
        ProjectTemplate projectTemplate = templateRepository.getTemplate(templateKey);

        if (projectTemplate != null) {
            return projectTemplate;
        }
        throw new TemplateNotFoundException(templateKey);
    }

    public List<ProjectTemplate> getProjectTemplates() {
        return templateRepository.getTemplateKeys()
                .stream()
                .parallel()
                .map(templateRepository::getTemplate)
                .sorted(Comparator.comparing(ProjectTemplate::getKey))
                .toList();
    }

    public List<TemplateParameter> getTemplateParameters(String selectedTemplateId) {
        return getTemplate(selectedTemplateId).getTemplateParameters();
    }

    public List<TemplateParameter> getModuleParameters(String selectedTemplateId, Set<String> selectedModuleIds) {
        return getTemplate(selectedTemplateId).getTemplateModules().stream()
                .filter(module -> selectedModuleIds.contains(module.getId()))
                .flatMap(module -> module.getModuleParameters().stream())
                .sorted(Comparator.comparing(TemplateParameter::getName))
                .toList();
    }

    public List<TemplateModule> getProjectModules(String selectedTemplateId) {
        return getTemplate(selectedTemplateId).getTemplateModules().stream()
                .sorted(Comparator.comparing(TemplateModule::getName))
                .toList();
    }
}
