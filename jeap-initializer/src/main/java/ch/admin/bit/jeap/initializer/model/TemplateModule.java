package ch.admin.bit.jeap.initializer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor // for jackson
public class TemplateModule {
    /**
     * ID of the module (i.e. 'object-storage'). Used to identify optional parts in the template related to the module
     */
    String id;
    /**
     * Human-readable name of the module
     */
    String name;
    String description;
    List<TemplateParameter> moduleParameters = new ArrayList<>();
}
