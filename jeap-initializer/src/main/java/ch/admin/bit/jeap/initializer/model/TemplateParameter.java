package ch.admin.bit.jeap.initializer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // for jackson
public class TemplateParameter {
    private String id;
    private String name;
    private String description;
}
