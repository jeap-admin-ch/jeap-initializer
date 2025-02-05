package ch.admin.bit.jeap.initializer.ui;

import ch.admin.bit.jeap.initializer.api.InitializerController;
import ch.admin.bit.jeap.initializer.generator.ProjectGenerator;
import ch.admin.bit.jeap.initializer.model.ProjectRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("DataFlowIssue")
@WebMvcTest(value = {WizardController.class, InitializerController.class}, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import(WizardControllerTestConfig.class)
@ActiveProfiles("test")
class WizardControllerTest {

    @MockBean
    private ProjectGenerator projectGenerator;
    @MockBean
    private CacheManager cacheManager;
    @Captor
    private ArgumentCaptor<ProjectRequest> projectRequestCaptor;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ResourceUrlProvider mvcResourceUrlProvider;

    @Test
    void wizardForm() throws Exception {
        // Step 1: Select Template
        mockMvc.perform(get("/wizard/step/select-template"))
                .andExpect(status().isOk());

        MvcResult selectTemplateResult = mockMvc.perform(post("/wizard/step/select-template")
                        .param("selectedTemplateId", "test-template"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/wizard/step/configure-template?*"))
                .andReturn();

        // Step 2: Configure Template
        String redirectedUrl = getRedirectedUrl(selectTemplateResult);
        String tsm = getModuleAttribute(selectTemplateResult, "templateSelectionModel");
        mockMvc.perform(get(redirectedUrl))
                .andExpect(status().isOk());

        MvcResult configureResult = mockMvc.perform(post("/wizard/step/configure-template")
                        .param("templateSelectionModel", tsm)
                        .param("awsAccountId", "123456789012")
                        .param("selectedModuleIds", "test-module"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/wizard/step/configure-modules?*"))
                .andReturn();

        // Step 3: Configure Modules
        String redirectedUrl3 = getRedirectedUrl(configureResult);
        String tcm = getModuleAttribute(configureResult, "templateConfigurationModel");
        mockMvc.perform(get(redirectedUrl3))
                .andExpect(status().isOk());

        MvcResult moduleConfigResult = mockMvc.perform(post("/wizard/step/configure-modules")
                        .param("templateSelectionModel", tsm)
                        .param("templateConfigurationModel", tcm)
                        .param("bucketName", "test-bucket"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/wizard/step/review?*"))
                .andReturn();

        // Step 4: Review selection
        String redirectedUrl4 = getRedirectedUrl(moduleConfigResult);
        String mcm = getModuleAttribute(moduleConfigResult, "moduleConfigurationModel");
        mockMvc.perform(get(redirectedUrl4))
                .andExpect(status().isOk());

        // Step 5: Generate project
        mockMvc.perform(post("/wizard/step/generate")
                        .param("templateSelectionModel", tsm)
                        .param("templateConfigurationModel", tcm)
                        .param("moduleConfigurationModel", mcm))
                .andExpect(status().isOk());

        verify(projectGenerator).generate(projectRequestCaptor.capture(), any());

        ProjectRequest projectRequest = projectRequestCaptor.getValue();
        assertThat(projectRequest.getTemplate())
                .isEqualTo("test-template");
        assertThat(projectRequest.getTemplateParameters())
                .containsEntry("awsAccountId", "123456789012");
        assertThat(projectRequest.getSelectedModuleIds())
                .containsExactly("test-module");
        assertThat(projectRequest.getSelectedTemplateModules())
                .hasSize(1)
                .first()
                .matches(module -> module.getModuleParameters().get("bucketName").equals("test-bucket"));
    }

    private static String getModuleAttribute(MvcResult selectTemplateResult, String templateSelectionModel) {
        return (String) selectTemplateResult.getModelAndView().getModel().get(templateSelectionModel);
    }

    private static String getRedirectedUrl(MvcResult selectTemplateResult) {
        return URLDecoder.decode(selectTemplateResult.getResponse().getRedirectedUrl(), UTF_8);
    }

    @Test
    void wizardForm_whenOptionalModuleIsNotSelected_thenShouldSkipModuleConfigurationStep() throws Exception {
        // Step 1: Select Template
        mockMvc.perform(get("/wizard/step/select-template"))
                .andExpect(status().isOk());

        MvcResult selectTemplateResult = mockMvc.perform(post("/wizard/step/select-template")
                        .param("selectedTemplateId", "test-template"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/wizard/step/configure-template?*"))
                .andReturn();

        // Step 2: Configure Template
        String redirectedUrl = getRedirectedUrl(selectTemplateResult);
        String tsm = getModuleAttribute(selectTemplateResult, "templateSelectionModel");
        mockMvc.perform(get(redirectedUrl))
                .andExpect(status().isOk());

        MvcResult configureResult = mockMvc.perform(post("/wizard/step/configure-template")
                        .param("templateSelectionModel", tsm)
                        .param("awsAccountId", "123456789012"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/wizard/step/configure-modules?*"))
                .andReturn();

        // Step 3: Configure Modules
        String redirectedUrl3 = getRedirectedUrl(configureResult);
        String tcm = getModuleAttribute(configureResult, "templateConfigurationModel");
        MvcResult moduleResult = mockMvc.perform(get(redirectedUrl3))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/wizard/step/review?*"))
                .andReturn();

        // Step 4: Review selection
        String redirectedUrl4 = getRedirectedUrl(moduleResult);
        String mcm = getModuleAttribute(moduleResult, "moduleConfigurationModel");
        mockMvc.perform(get(redirectedUrl4))
                .andExpect(status().isOk());

        // Step 5: Generate project
        mockMvc.perform(post("/wizard/step/generate")
                        .param("templateSelectionModel", tsm)
                        .param("templateConfigurationModel", tcm)
                        .param("moduleConfigurationModel", mcm))
                .andExpect(status().isOk());

        verify(projectGenerator).generate(projectRequestCaptor.capture(), any());

        ProjectRequest projectRequest = projectRequestCaptor.getValue();
        assertThat(projectRequest.getTemplate())
                .isEqualTo("test-template");
        assertThat(projectRequest.getTemplateParameters())
                .containsEntry("awsAccountId", "123456789012");
        assertThat(projectRequest.getSelectedModuleIds())
                .isEmpty();
    }
}
