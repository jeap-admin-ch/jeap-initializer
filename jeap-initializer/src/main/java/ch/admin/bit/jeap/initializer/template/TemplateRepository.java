package ch.admin.bit.jeap.initializer.template;


import ch.admin.bit.jeap.initializer.model.ProjectTemplate;

import java.util.Set;


public interface TemplateRepository {

    Set<String> getTemplateKeys();

    ProjectTemplate getTemplate(String key);

}
