package ch.admin.bit.jeap.initializer.config;

import java.util.Set;

public class TemplateModuleNotFoundException extends RuntimeException {

    public TemplateModuleNotFoundException(String id, Set<String> validModuleIds) {
        super("Template module '%s' is not known in template (valid modules IDs: %s)"
                .formatted(id, validModuleIds));
    }

}
