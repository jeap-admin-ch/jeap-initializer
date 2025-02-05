package ch.admin.bit.jeap.initializer.ui.model;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Valid
public class ModuleConfigurationModel {

    private Map<String, String> moduleParameterValues = new HashMap<>();
}
