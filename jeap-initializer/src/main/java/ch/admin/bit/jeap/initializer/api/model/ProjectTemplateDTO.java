package ch.admin.bit.jeap.initializer.api.model;

import ch.admin.bit.jeap.initializer.model.ProjectTemplate;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;

import java.util.List;

public record ProjectTemplateDTO(String key, String name, String description, String platform,
                                 List<TemplateParameter> templateParameters,
                                 List<TemplateModuleDTO> modules) {

    public static ProjectTemplateDTO from(ProjectTemplate projectTemplate) {
        return new ProjectTemplateDTO(projectTemplate.getKey(),
                projectTemplate.getName(),
                projectTemplate.getDescription(),
                projectTemplate.getPlatform(),
                projectTemplate.getTemplateParameters(),
                projectTemplate.getTemplateModules().stream()
                        .map(TemplateModuleDTO::from)
                        .toList());
    }

}
