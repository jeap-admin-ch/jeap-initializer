package ch.admin.bit.jeap.initializer.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FrontendControllerTest {

    private FrontendController frontendController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        frontendController = new FrontendController();
        mockMvc = MockMvcBuilders.standaloneSetup(frontendController).build();
    }

    @Test
    void redirectIndex_returnsExpectedRedirectString() {
        // Act
        String view = frontendController.redirectIndex();
        // Assert
        assertThat(view).isEqualTo("redirect:/wizard/step/select-platform");
    }

    @Test
    void getRootPath_redirectsToWizardStepSelectApp() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wizard/step/select-platform"));
    }
}
