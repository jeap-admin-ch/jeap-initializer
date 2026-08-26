# Getting Started

The jEAP Initializer is a Spring Boot application (not a plain library) that generates ready-to-use project
codebases from Git-hosted templates. You run it as a service, configure one or more templates, and then either
call its REST API or use its built-in wizard UI to generate a downloadable project archive.

## Add the dependency

```xml

<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-initializer</artifactId>
    <version>...</version>
</dependency>
```

The main class is `ch.admin.bit.jeap.initializer.Application`, a Spring Boot application. Most consumers simply
depend on this artifact directly and run it as-is; see [Extending](extending.md) if you need custom behaviour.

## Configure at least one template

Every template the initializer can generate from must be declared in the application configuration. See
[Configuration](configuration.md) for the full property reference; minimally you need a template key and a Git
repository URL:

```yaml
jeap:
  initializer:
    templates:
      my-template:
        repository-configuration:
          url: https://bitbucket.example.org/scm/my-project/my-template.git
```

The template repository itself must carry an `initializer.yaml` metadata file at its root describing its name,
parameters and optional modules — see [Template Authoring](template-authoring.md).

## Generate a project

Once the application is running and at least one template is configured, a project can be generated either via
the wizard UI or the REST API.

### Wizard UI

Open the application's root URL in a browser. The wizard walks through platform selection (if configured),
template selection, template parameters, optional module selection and module parameters, then triggers
generation and downloads the resulting project as a `.tar.gz` archive.

### REST API

Call `POST <application-context-path>/api/generate` with a JSON body describing the desired project:

```json
{
  "template": "my-template",
  "applicationName": "My jEAP Project",
  "basePackage": "ch.admin.bit.jme",
  "systemName": "jme",
  "department": "BIT",
  "artifactId": "my-app",
  "groupId": "ch.admin.bit",
  "templateParameters": {
    "additionalProp1": "string"
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

The response is a `.tar.gz` archive of the generated project. A Swagger UI describing the full request/response
schema is available under `<application-context-path>/swagger-ui.html`.

| Field                    | Required/Optional | Description                                                                         |
|--------------------------|--------------------|--------------------------------------------------------------------------------------|
| `template`               | Required           | The template key; must match one of the configured templates.                        |
| `applicationName`        | Optional           | The application name. Defaults to `jEAP Project`.                                     |
| `basePackage`            | Required           | The base package to use when generating Java classes.                                |
| `groupId`                | Required           | The Maven groupId to use in the generated project.                                    |
| `artifactId`             | Required           | The Maven artifactId to use in the generated project.                                 |
| `templateParameters`     | Optional           | Additional parameters required by the template; see the template's `initializer.yaml`.|
| `systemName`             | Required           | Abbreviation of the system name.                                                      |
| `department`             | Optional           | Abbreviation of the federal department. Defaults to `BIT`.                            |
| `selectedTemplateModules`| Optional           | Optional modules to include, each with its own `moduleParameters`.                    |

## See also

- [Architecture](architecture.md) — how generation works internally (contributors, Git cloning, ordering)
- [Configuration](configuration.md) — full property reference
- [Template Authoring](template-authoring.md) — writing `initializer.yaml` and marking module-specific content
- [Extending](extending.md) — adding custom `ProjectContributor` beans
