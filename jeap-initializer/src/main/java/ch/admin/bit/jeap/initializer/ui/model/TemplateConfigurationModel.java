package ch.admin.bit.jeap.initializer.ui.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Valid
public class TemplateConfigurationModel {

    private Set<String> selectedModuleIds = new HashSet<>();

    @NotBlank(message = "Please enter a system name")
    private String systemName = "jme";
    @NotBlank(message = "Please enter a department name")
    private String department = "BIT";
    @NotBlank(message = "Please enter a human-readable application name")
    private String applicationName = "jEAP Project";
    @NotBlank(message = "Please enter the base package")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z0-9]+)*$", message = "Please enter a valid package name")
    private String basePackage = "ch.admin.bit.jme";
    @NotBlank(message = "Please enter the Maven artifact ID")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*(-[a-zA-Z0-9]+)*$", message = "Please enter a valid artifact ID")
    private String artifactId = "my-app";
    @NotBlank(message = "Please enter the Maven artifact's group ID")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z0-9]+)*$", message = "Please enter a valid group ID")
    private String groupId = "ch.admin.bit";

    private Map<String, String> templateParameterValues = new HashMap<>();
}
