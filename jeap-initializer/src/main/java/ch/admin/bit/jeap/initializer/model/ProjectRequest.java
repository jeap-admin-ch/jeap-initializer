package ch.admin.bit.jeap.initializer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.*;

import static java.util.stream.Collectors.toSet;

@Schema(description = "Request body for generating a project")
@Data
public class ProjectRequest {

    @Schema(description = "Name of the template to use", defaultValue = "jeap-scs")
    private String template = "jeap-scs";

    @Schema(description = "System name to use (i.e. jme, via, wvs)", defaultValue = "jme")
    private String systemName = "jme";

    @Schema(description = "Federal Department (i.e. BIT, BAZG, ...", defaultValue = "BIT")
    private String department = "BIT";

    @Schema(description = "Name of the application to generate", defaultValue = "My jEAP Project")
    private String applicationName = "jEAP Project";

    @Schema(description = "Base Java package to use", defaultValue = "ch.admin.bit.jme")
    private String basePackage = "ch.admin.bit.jme";

    @Schema(description = "Maven ArtifactId to use", defaultValue = "my-app")
    private String artifactId = "my-app";

    @Schema(description = "Maven GroupId to use", defaultValue = "ch.admin.bit")
    private String groupId = "ch.admin.bit";

    @Schema(description = "Additional template parameters")
    private Map<String, String> templateParameters = new HashMap<>();

    @Schema(description = "Optional template module selection with their module parameter values")
    private List<SelectedModule> selectedTemplateModules = new ArrayList<>();

    public Set<String> getSelectedModuleIds() {
        if (selectedTemplateModules == null) {
            return Set.of();
        }
        return selectedTemplateModules.stream()
                .map(SelectedModule::getId)
                .collect(toSet());
    }

    public String getParameterValue(String parameterName) {
        if (templateParameters.containsKey(parameterName)) {
            return templateParameters.get(parameterName);
        }
        return selectedTemplateModules.stream()
                .filter(module -> module.getModuleParameters().containsKey(parameterName))
                .map(module -> module.getModuleParameters().get(parameterName))
                .findFirst()
                .orElseGet(() -> requestParameterValue(parameterName));
    }

    private String requestParameterValue(String parameterName) {
        Map<String, String> requestParameters = Map.of(
                "template", template,
                "systemName", systemName,
                "department", department,
                "applicationName", applicationName,
                "basePackage", basePackage,
                "artifactId", artifactId,
                "groupId", groupId
        );
        if (requestParameters.containsKey(parameterName)) {
            return requestParameters.get(parameterName);
        }
        return "<missing value for parameter %s>".formatted(parameterName);
    }
}
