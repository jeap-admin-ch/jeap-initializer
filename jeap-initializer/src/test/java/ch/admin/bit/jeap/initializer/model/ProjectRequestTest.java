package ch.admin.bit.jeap.initializer.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectRequestTest {

    @Test
    void getSelectedModuleIds_withModules_shouldReturnModuleIds() {
        ProjectRequest request = new ProjectRequest();
        request.setSelectedTemplateModules(List.of(
                new SelectedModule("module1", Map.of()),
                new SelectedModule("module2", Map.of())
        ));

        Set<String> moduleIds = request.getSelectedModuleIds();

        assertThat(moduleIds).containsExactlyInAnyOrder("module1", "module2");
    }

    @Test
    void getSelectedModuleIds_withNoModules_shouldReturnEmptySet() {
        ProjectRequest request = new ProjectRequest();

        Set<String> moduleIds = request.getSelectedModuleIds();

        assertThat(moduleIds).isEmpty();
    }

    @Test
    void getParameterValue_withTemplateParameter_shouldReturnTemplateParameterValue() {
        ProjectRequest request = new ProjectRequest();
        request.setTemplateParameters(Map.of("param1", "value1"));

        String value = request.getParameterValue("param1");

        assertThat(value).isEqualTo("value1");
    }

    @Test
    void getParameterValue_withModuleParameter_shouldReturnModuleParameterValue() {
        ProjectRequest request = new ProjectRequest();
        request.setSelectedTemplateModules(List.of(
                new SelectedModule("module1", Map.of("param1", "value1"))
        ));

        String value = request.getParameterValue("param1");

        assertThat(value).isEqualTo("value1");
    }

    @Test
    void getParameterValue_withRequestParameter_shouldReturnRequestParameterValue() {
        ProjectRequest request = new ProjectRequest();
        request.setApplicationName("MyApp");

        String value = request.getParameterValue("applicationName");

        assertThat(value).isEqualTo("MyApp");
    }

    @Test
    void getParameterValue_withMissingParameter_shouldReturnMissingValueMessage() {
        ProjectRequest request = new ProjectRequest();

        String value = request.getParameterValue("nonExistentParam");

        assertThat(value).isEqualTo("<missing value for parameter nonExistentParam>");
    }
}
