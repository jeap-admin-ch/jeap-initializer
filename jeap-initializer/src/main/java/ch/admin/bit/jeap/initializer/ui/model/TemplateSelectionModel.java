package ch.admin.bit.jeap.initializer.ui.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Valid
public class TemplateSelectionModel {

    @NotBlank(message = "Please select a template")
    private String selectedTemplateId;
}
