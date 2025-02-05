package ch.admin.bit.jeap.initializer.ui;

import ch.admin.bit.jeap.initializer.api.InitializerController;
import ch.admin.bit.jeap.initializer.api.model.ProjectTemplateDTO;
import ch.admin.bit.jeap.initializer.api.model.TemplateModuleDTO;
import ch.admin.bit.jeap.initializer.model.TemplateParameter;
import ch.admin.bit.jeap.initializer.template.TemplateService;
import ch.admin.bit.jeap.initializer.ui.model.ModuleConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateConfigurationModel;
import ch.admin.bit.jeap.initializer.ui.model.TemplateSelectionModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wizard")
public class WizardController {

    // View rendering attributes
    static final String TEMPLATE_LIST = "templateList";
    static final String TEMPLATE_PARAMETERS = "templateParameters";
    static final String MODULE_LIST = "moduleList";
    // Models
    static final String TEMPLATE_SELECTION_MODEL = "templateSelectionModel";
    static final String TEMPLATE_CONFIGURATION_MODEL = "templateConfigurationModel";
    static final String MODULE_CONFIGURATION_MODEL = "moduleConfigurationModel";

    private final ProjectRequestFactory projectRequestFactory;
    private final TemplateService templateService;
    private final InitializerController initializerController;

    @GetMapping("/step/select-template")
    public String showStepSelectTemplate(Model model) {
        prepareSelectionModelForRendering(model);
        return "wizard-step-select-template";
    }

    @PostMapping("/step/select-template")
    public String processStepSelectTemplate(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareSelectionModelForRendering(model);
            return "wizard-step-select-template";
        }
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        return "redirect:/wizard/step/configure-template";
    }

    private void prepareSelectionModelForRendering(Model model) {
        model.addAttribute(TEMPLATE_LIST, templateService.getProjectTemplates().stream()
                .map(ProjectTemplateDTO::from).toList());
        if (!model.containsAttribute(TEMPLATE_SELECTION_MODEL)) {
            model.addAttribute(TEMPLATE_SELECTION_MODEL, new TemplateSelectionModel());
        }
    }

    @GetMapping("/step/configure-template")
    public String showStepConfigureTemplate(@ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                            Model model) {
        model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);

        prepareTemplateConfigurationModelForRendering(model, templateSelectionModel);

        return "wizard-step-configure-template";
    }

    @PostMapping("/step/configure-template")
    public String processStepConfigureTemplate(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                               @Valid @ModelAttribute(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                               BindingResult bindingResult, HttpServletRequest request,
                                               Model model, RedirectAttributes redirectAttributes) {
        var templateParameters = templateService.getTemplateParameters(templateSelectionModel.getSelectedTemplateId());
        templateParameters.forEach(templateParameter ->
                templateConfigurationModel.getTemplateParameterValues().put(templateParameter.getId(), request.getParameter(templateParameter.getId())));

        if (bindingResult.hasErrors()) {
            prepareTemplateConfigurationModelForRendering(model, templateSelectionModel);
            return "wizard-step-configure-template";
        }

        redirectAttributes.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        return "redirect:/wizard/step/configure-modules";
    }

    private void prepareTemplateConfigurationModelForRendering(Model model, TemplateSelectionModel templateSelectionModel) {
        String selectedTemplateId = templateSelectionModel.getSelectedTemplateId();
        model.addAttribute(TEMPLATE_PARAMETERS, templateService.getTemplateParameters(selectedTemplateId));
        model.addAttribute(MODULE_LIST, templateService.getProjectModules(selectedTemplateId)
                .stream().map(TemplateModuleDTO::from).toList());
        if (!model.containsAttribute(TEMPLATE_CONFIGURATION_MODEL)) {
            model.addAttribute(TEMPLATE_CONFIGURATION_MODEL, new TemplateConfigurationModel());
        }
    }

    @GetMapping("/step/configure-modules")
    public String showStepConfigureModules(@RequestParam(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                           @RequestParam(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                           Model model, RedirectAttributes redirectAttributes) {

        var moduleParameters = templateService.getModuleParameters(
                templateSelectionModel.getSelectedTemplateId(), templateConfigurationModel.getSelectedModuleIds());

        if (moduleParameters.isEmpty()) {
            redirectAttributes.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
            redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
            redirectAttributes.addAttribute(MODULE_CONFIGURATION_MODEL, new ModuleConfigurationModel());
            return "redirect:/wizard/step/review";
        }

        prepareModuleConfigurationModelForRendering(model, moduleParameters);
        model.addAttribute(MODULE_CONFIGURATION_MODEL, new ModuleConfigurationModel());
        model.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);

        return "wizard-step-configure-modules";
    }

    @PostMapping("/step/configure-modules")
    public String processStepConfigureModules(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                              @Valid @ModelAttribute(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                              @Valid @ModelAttribute(MODULE_CONFIGURATION_MODEL) ModuleConfigurationModel moduleConfigurationModel,
                                              BindingResult bindingResult, HttpServletRequest request,
                                              Model model, RedirectAttributes redirectAttributes) {

        var moduleParameters = templateService.getModuleParameters(
                templateSelectionModel.getSelectedTemplateId(), templateConfigurationModel.getSelectedModuleIds());
        moduleParameters.forEach(moduleParameter ->
                moduleConfigurationModel.getModuleParameterValues().put(moduleParameter.getId(), request.getParameter(moduleParameter.getId())));

        if (bindingResult.hasErrors()) {
            prepareModuleConfigurationModelForRendering(model, moduleParameters);
            return "wizard-step-configure-modules";
        }

        redirectAttributes.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        redirectAttributes.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        redirectAttributes.addAttribute(MODULE_CONFIGURATION_MODEL, moduleConfigurationModel);

        return "redirect:/wizard/step/review";
    }

    private static void prepareModuleConfigurationModelForRendering(Model model, List<TemplateParameter> moduleParameters) {
        model.addAttribute("moduleParameters", moduleParameters);
    }

    @GetMapping("/step/review")
    public String review(@RequestParam(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                         @RequestParam(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                         @RequestParam(MODULE_CONFIGURATION_MODEL) ModuleConfigurationModel moduleConfigurationModel,
                         Model model) {
        model.addAttribute(TEMPLATE_CONFIGURATION_MODEL, templateConfigurationModel);
        model.addAttribute(TEMPLATE_SELECTION_MODEL, templateSelectionModel);
        model.addAttribute(MODULE_CONFIGURATION_MODEL, moduleConfigurationModel);
        return "wizard-step-review";
    }

    @PostMapping("/step/generate")
    public ResponseEntity<InputStreamResource> generate(@Valid @ModelAttribute(TEMPLATE_SELECTION_MODEL) TemplateSelectionModel templateSelectionModel,
                                                        @Valid @ModelAttribute(TEMPLATE_CONFIGURATION_MODEL) TemplateConfigurationModel templateConfigurationModel,
                                                        @Valid @ModelAttribute(MODULE_CONFIGURATION_MODEL) ModuleConfigurationModel moduleConfigurationModel) throws IOException {
        var projectRequest = projectRequestFactory.createProjectRequest(
                templateSelectionModel, templateConfigurationModel, moduleConfigurationModel);
        return initializerController.generate(projectRequest);
    }

    @PostMapping("/reset")
    public String reset() {
        return "redirect:/wizard/step/select-template";
    }
}
