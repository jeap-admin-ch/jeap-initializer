package ch.admin.bit.jeap.initializer.model;

import lombok.Data;
import lombok.ToString;

@Data
public class GitRepositoryConfiguration {
    private String url;
    private String reference = "master";
    @ToString.Exclude
    private String user;
    @ToString.Exclude
    private String password;
}
