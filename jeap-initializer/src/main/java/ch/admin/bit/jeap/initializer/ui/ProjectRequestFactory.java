package ch.admin.bit.jeap.initializer.ui;

import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import ch.admin.bit.jeap.initializer.model.SelectedModule;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import ch.admin.bit.jeap.initializer.template.TemplateService;
import ch.admin.bit.jeap.initializer.ui.model.ModuleConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateSelectionModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
@RequiredArgsConstructor
class ProjectRequestFactory {

    private final TemplateService templateService;

    ProjectRequest createProjectRequest(TemplateSelectionModel selectionModel,
                                        TemplateConfigurationModel templateConfigurationModel,
                                        ModuleConfigurationModel moduleConfigurationModel) {

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setTemplate(selectionModel.getSelectedTemplateId());
        projectRequest.setSystemName(templateConfigurationModel.getSystemName());
        projectRequest.setDepartment(templateConfigurationModel.getDepartment());
        projectRequest.setApplicationName(templateConfigurationModel.getApplicationName());
        projectRequest.setArtifactId(templateConfigurationModel.getArtifactId());
        projectRequest.setGroupId(templateConfigurationModel.getGroupId());
        projectRequest.setBasePackage(templateConfigurationModel.getBasePackage());
        projectRequest.setTemplateParameters(templateConfigurationModel.getTemplateParameterValues());
        projectRequest.setSelectedTemplateModules(createSelectedModules(selectionModel.getSelectedTemplateId(), templateConfigurationModel, moduleConfigurationModel));
        return projectRequest;
    }

    private List<SelectedModule> createSelectedModules(String selectedTemplateId,
                                                       TemplateConfigurationModel templateConfigurationModel,
                                                       ModuleConfigurationModel moduleConfigurationModel) {
        return templateConfigurationModel.getSelectedModuleIds().stream().map(moduleId -> {
            Set<String> parameterIdsForModule =
                    templateService.getModuleParameters(selectedTemplateId, templateConfigurationModel.getSelectedModuleIds()).stream()
                            .map(TemplateParameter::getId)
                            .collect(toSet());

            return createSelectedModule(moduleConfigurationModel, moduleId, parameterIdsForModule);
        }).toList();
    }

    private static SelectedModule createSelectedModule(ModuleConfigurationModel moduleConfigurationModel, String moduleId, Set<String> parameterIdsForModule) {
        SelectedModule selectedModule = new SelectedModule();
        selectedModule.setId(moduleId);
        Map<String, String> moduleParameters = moduleConfigurationModel.getModuleParameterValues().entrySet().stream()
                .filter(entry -> parameterIdsForModule.contains(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        selectedModule.setModuleParameters(moduleParameters);
        return selectedModule;
    }
}
