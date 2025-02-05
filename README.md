## jEAP Initializer Library

This library enables to generate ready-to-use codebases for bootstrapping projects. Unlike other
initializer-type tools, it creates code based on existing projects (referred to as templates) hosted
in Git repositories. This approach offers several advantages:

- Templates can evolve independently of the initializer.
- Template functionality can be verified through tests and pipelines
- Additional templates can be added via configuration

### Integration

This library is meant to be integrated into a project, so the first step is to add the corresponding dependency:

```xml

<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-initializer</artifactId>
    <version>...</version>
</dependency>
```

The main class is `ch.admin.bit.jeap.initializer.Application`, which can be started as a Spring Boot Application.

### Configuration

Available templates have to be added to the application configuration. This can be done by adding the
following properties:

```yaml
jeap:
  initializer:
    templates:
      template1: ## Template key
        repository-configuration:
          url: <<url_to_project_template_repo>>
          reference: master
          user: user
          password: password
        git-ops-repository-configuration:
          url: <<url_to_gitops_repo>>
          reference: master
          user: user
          password: password
      template2: ## Template key
        repository-configuration:
          url: <<url_to_another_project_template_repo>>
```

The snippet above defines two templates with the keys 'template1' and 'template2'. These keys are used to select the
template for generating the code. For each template the following properties can be set:

| Name                                       | Required/Optional | Description                                                         | Example                                                      |
|--------------------------------------------|-------------------|---------------------------------------------------------------------|--------------------------------------------------------------|
| repository-configuration.url               | Required          | The URL to the repository containing the template source code       | 'https://github.com/some-org/some-project-template-repo.git' |
| repository-configuration.reference         | Optional          | Git ref containing the code in Git repository. Defaults to 'master' | 'origin/feature/other-branch'                                |
| repository-configuration.user              | Optional          | User name to use for Git authentication                             | 'user'                                                       |
| repository-configuration.password          | Optional          | Password to use for Git authentication                              | 'password'                                                   |
| git-ops-repository-configuration.url       | Optional          | The URL to the repository containing the GitOps source code         | 'https://github.com/some-org/some-gitops-repo.git'           |
| git-ops-repository-configuration.reference | Optional          | Git ref containing the code in Git repository. Defaults to 'master' | 'origin/feature/other-branch'                                |
| git-ops-repository-configuration.user      | Optional          | User name to use for Git authentication                             | 'user'                                                       |
| git-ops-repository-configuration.password  | Optional          | Password to use for Git authentication                              | 'password'                                                   |

### Defining template metadata and parameters

Each template must have a metadata file that describes the template and its parameters. This file must be named
initializer.yaml and be placed at the root of the template repository. The file structure is as follows:

```yaml
name: "jEAP Example Template"
description: "Provides an instance of a jEAP example app"
# These values will be replaced with actual values provided by the user generating an app from the template
base-package: ch.admin.bit.jme
system-name: jme
artifact-id: jme-example-app
group-id: ch.admin.bit.jme

# Define the parameters that can be set when generating a project from this template
template-parameters:
  - id: awsAccountId
    name: AWS account id
    description: AWS Development Environment Account ID

# Define the optional modules that can be selected when generating a project from this template
template-modules:
  - id: object-storage
    name: Object Storage (S3)
    description: Provides dependencies and configuration for integrating with an AWS S3 object storage
    module-parameters:
      - id: bucket-name
        name: Bucket Name
        description: Name of the bucket used to store/read data
```

### Usage

Once the application instantiating the library is running and properly configured, projects can be generated by calling
the endpoint <application-context-path>/api/generate with an HTTP POST and the following request body:

```json
{
  "template": "jeap-scs",
  "applicationName": "My jEAP Project",
  "basePackage": "ch.admin.bit.jme",
  "systemName": "jme",
  "department": "BIT",
  "artifactId": "my-app",
  "groupId": "ch.admin.bit",
  "templateParameters": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "selectedTemplateModules": [
    {
      "id": "module1",
      "moduleParameters": {
        "moduleProp1": "string"
      }
    }
  ]
}
```

In addition to that, a Swagger UI is provided under <application-context-path>/swagger-ui.html. 

The values will be used to generate the code with the corresponding configuration:

| Name                    | Required/Optional | Description                                                                         |
|-------------------------|-------------------|-------------------------------------------------------------------------------------|
| template                | Required          | The template key. It must match one of the configured ones.                         |
| applicationName         | Optional          | The application name. Defaults to 'jEAP Project'.                                   |
| basePackage             | Required          | The base package to use when generating Java classes.                               |
| groupId                 | Required          | The Maven groupId to use in the generated project.                                  |
| artifactId              | Required          | The Maven artifactId to use in the generated project.                               |
| templateParameters      | Optional          | Additional parameters required by templates can be added here.                      |
| system-name             | Required          | Abbreviation of the system name.                                                    |
| department              | Optional          | Abbreviation of the federal department. Defaults to 'BIT'.                          |
| selectedTemplateModules | Optional          | Optional modules that can be selected when generating a project from this template. |

### Extending with custom ProjectContributors

Should you require additional logic in your initializer, then you may define new Beans that implement the
`ch.admin.bit.jeap.initializer.contributor.ProjectContributor` interface. When present in the ApplicationContext, they
will be picked up by the initializer library.

By default, a component scan is enabled for classes under `ch.admin.bit.jeap.initializer`. If you want to define beans
on another package, you can use for instance the methods described
in https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html.

### Proxy configuration for local development

As local development is done in environments connected to the federal network, JGit requires additional configuration so
that it can reach github.com through the proxy. This configuration takes place automatically when the Spring profile
`local` is active, and it will set the proxy configuration to the default one. See
`ch.admin.bit.jeap.initializer.config.JGitProxyConfiguration`.

The JGit proxy configuration can be manually changed by setting the two following variables:

- ch.admin.bit.jeap.initializer.config.jgit.proxy.host (default value: proxy-bvcol.admin.ch)
- ch.admin.bit.jeap.initializer.config.jgit.proxy.port (default value: 8080)

## Note

This repository is part of the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
