package ch.admin.bit.jeap.initializer.ui.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Valid
public class PlatformSelectionModel {

    @NotBlank(message = "Please select a platform")
    private String selectedPlatformId;
}
