package ch.admin.bit.jeap.initializer.api.model;

import ch.admin.bit.jeap.initializer.model.TemplateModule;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;

import java.util.List;

public record TemplateModuleDTO(String id, String name, String description, List<TemplateParameter> moduleParameters) {

    public static TemplateModuleDTO from(TemplateModule templateModule) {
        return new TemplateModuleDTO(templateModule.getId(),
                templateModule.getName(),
                templateModule.getDescription(),
                templateModule.getModuleParameters());
    }
}
