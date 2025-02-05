package ch.admin.bit.jeap.initializer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Schema(description = "Selected optional template module with module parameter values")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectedModule {

    @Schema(description = "Selected module's ID")
    private String id;

    @Schema(description = "Module parameters")
    private Map<String, String> moduleParameters = new HashMap<>();
}
