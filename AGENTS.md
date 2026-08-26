# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* this application (configuring
templates, calling the API, authoring templates), read [README.md](README.md) and the [docs/](docs/) folder
instead.

## Project

jEAP Initializer is a Spring Boot application (single module, not a library with multiple artifacts) that
generates ready-to-use project codebases from Git-hosted templates. Given a template key and parameters, it
clones the template repository, runs a chain of `ProjectContributor`s over the checkout to rename/parameterize it,
and returns the result as a `.tar.gz` archive — via a REST API (`POST /api/generate`) or a server-rendered wizard
UI.

## Repository layout

```
pom.xml                          # Parent POM (packaging=pom); declares the single jeap-initializer module
jeap-initializer/                # The Spring Boot application module
  src/main/java/ch/admin/bit/jeap/initializer/
    Application.java             # Spring Boot entry point
    config/                      # JeapInitializerProperties, WebSecurityConfig, OpenApiConfig, exceptions
    model/                       # ProjectTemplate, Platform, ProjectRequest, TemplateParameter, TemplateModule, ...
    template/                    # TemplateService, TemplateRepository, CachingTemplateRepository (parses initializer.yaml)
    git/                         # GitService / DefaultGitService (JGit-based cloning)
    generator/                   # ProjectGenerator: orchestrates cloning + contributor chain + Git re-init
    contributor/                 # ProjectContributor interface + all built-in contributors
    api/                         # InitializerController (REST API) + DTOs
    ui/                          # WizardController, FrontendController (server-rendered wizard)
    util/                        # FileUtils, TarGzipUtils, FileProcessor
  src/main/resources/templates/  # Thymeleaf templates for the wizard UI
Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE
```

## Build & test

```bash
./mvnw -pl jeap-initializer -am install    # build the module and its dependencies
./mvnw verify                              # full build incl. tests
./mvnw -pl jeap-initializer test           # unit tests only
```

- Parent: `ch.admin.bit.jeap:jeap-spring-boot-parent`.
- Contributor tests typically build a minimal `ProjectTemplate`/`ProjectRequest` and a temp directory, run
  `contribute(...)`, then assert on the resulting files — see the existing `*ContributorTest` classes for the
  pattern.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.initializer...`.
- Configuration properties use the prefix `jeap.initializer.*` (see `JeapInitializerProperties`).
- New generation-pipeline steps are added as `@Component`-annotated `ProjectContributor` beans, not by modifying
  `ProjectGenerator` directly; use `getOrder()` to position them relative to existing contributors (see the order
  table in [docs/architecture.md](docs/architecture.md)).
- Template metadata markers (`START/END MODULE <id>`, `START/END INITIALIZER DELETE`, `INITIALIZER PARAMETER ...
  VALUE ...`, `MODULE-SPECIFIC FILE FOR MODULE <id>`, `*.initializer-template`) are a public contract used by
  external template repositories — changing their syntax is a breaking change; document any addition in
  [docs/template-authoring.md](docs/template-authoring.md).

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per file) and
the documentation index in the README.

- Pages must be valid MDX (Docusaurus renders every `.md` as MDX) and any Mermaid diagrams must use correct
  Mermaid syntax — see the [writing principles](https://github.com/jeap-admin-ch/jeap/blob/master/docs/documenting-jeap.md#writing-principles).
- There is no standalone linter for this; validate by actually building the docs site locally against this
  checkout, using the [site repository](https://github.com/jeap-admin-ch/jeap-admin-ch.github.io)'s
  `preview.sh --local <path-to-this-repo> --no-autodiscover` (production build, catches MDX/Mermaid syntax errors
  and broken links) or `dev.sh` for a faster hot-reload check.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- `setPomVersions.sh` updates the version across all module POMs.
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POMs.
- Always keep the -SNAPSHOT postfix in the POMs, CI will remove it when releasing a version. Do not use the
  SNAPSHOT postfix in other places (CHANGELOG, publiccode.yml etc.)
- Keep changelog entries concise and to the point, follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional commits
  (for example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog, and update version/date in `publiccode.yml`.
- When the version on a feature branch has not yet been bumped compared to master, ask the user if a major, minor
  or patch version bump should be performed, and update the version accordingly.
