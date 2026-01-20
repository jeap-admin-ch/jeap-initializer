package ch.admin.bit.jeap.initializer.template;


import ch.admin.bit.jeap.initializer.model.Platform;
import ch.admin.bit.jeap.initializer.model.ProjectTemplate;

import java.util.Set;


public interface TemplateRepository {

    Set<String> getTemplateKeys();

    ProjectTemplate getTemplate(String key);

    /**
     * Get platform configuration (name, description) from the configuration
     *
     * @param key Platform key
     * @return Platform object with name and description, or a fallback instance using the key as name if not found
     */
    Platform getConfiguredPlatform(String key);

}
