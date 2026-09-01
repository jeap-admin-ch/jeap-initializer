# Configuration

## Templates and platforms

Available templates are declared under `jeap.initializer.templates`, keyed by an arbitrary template key that is
used in generation requests to select the template. Platforms (used to group templates in the wizard UI) are
declared under `jeap.initializer.platforms` and referenced from a template's `platform` property in its
`initializer.yaml` (see [Template Authoring](template-authoring.md)); platforms are optional.

This is ordinary Spring configuration, so it can live in the instance's `application.yml` or be
externalized to the platform's config source (environment variables, a config server, AWS AppConfig,
…) — useful for adding or retargeting templates without rebuilding the instance.

```yaml
jeap:
  initializer:
    platforms:
      my-cloud:
        name: Cloud Platform
        description: Some Cloud Platform
      my-container-platform:
        name: Container Platform
        description: Some Container Platform
    templates:
      template1: # Template key, referenced as "template" in generation requests
        repository-configuration:
          url: https://bitbucket.example.org/scm/my-project/my-project-template.git
          reference: master
          user: user
          password: password
        git-ops-repository-configuration:
          url: https://bitbucket.example.org/scm/my-project/my-gitops-repo.git
          reference: master
          user: user
          password: password
      template2:
        repository-configuration:
          url: https://bitbucket.example.org/scm/my-project/another-template.git
```

The snippet above defines two templates with the keys `template1` and `template2`. On startup,
`JeapInitializerProperties` validates that every configured platform has a `name` and `description`, and that
every configured template has a `repository-configuration.url` — the application fails to start otherwise.

### Template properties

| Name                                        | Required/Optional | Description                                                          | Example                                                               |
|----------------------------------------------|---------------------|------------------------------------------------------------------------|--------------------------------------------------------------------------|
| `repository-configuration.url`                | Required           | The URL to the Git repository containing the template source code.  | `https://bitbucket.example.org/scm/my-project/my-project-template.git` |
| `repository-configuration.reference`          | Optional           | Git ref to check out. Defaults to `master`.                          | `origin/feature/other-branch`                                          |
| `repository-configuration.user`               | Optional           | User name for Git authentication.                                    | `user`                                                                  |
| `repository-configuration.password`           | Optional           | Password/token for Git authentication.                               | `password`                                                              |
| `git-ops-repository-configuration.url`        | Optional           | The URL to a companion GitOps repository, checked out to a `gitops/` subfolder. | `https://bitbucket.example.org/scm/my-project/my-gitops-repo.git` |
| `git-ops-repository-configuration.reference`  | Optional           | Git ref for the GitOps repository. Defaults to `master`.              | `origin/feature/other-branch`                                          |
| `git-ops-repository-configuration.user`       | Optional           | User name for GitOps repository authentication.                      | `user`                                                                  |
| `git-ops-repository-configuration.password`   | Optional           | Password/token for GitOps repository authentication.                 | `password`                                                              |

## Other properties

| Property                                | Default                                                          | Description                                                                                       |
|-------------------------------------------|---------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `jeap.initializer.template-cache-duration` | `4h`                                                             | How long a parsed `ProjectTemplate` (from `initializer.yaml`) is cached before being re-fetched from Git. `POST /api/cache/reset` clears the cache on demand (see [Getting Started](getting-started.md#rest-api)). |
| `jeap.initializer.source-files-pattern`    | `CODEOWNERS\|Dockerfile\|Jenkinsfile.*\|(.+\.(md\|html\|css\|java\|xml\|yaml\|yml\|properties\|json\|conf\|ts))` (case-insensitive) | Regex matching which files are considered "source files" eligible for marker-based contributors (`CodeRemoverContributor`, `ParameterReplacementContributor`, `TemplateModuleContributor`). |

## Proxy configuration for local development

When developing behind the federal network, JGit needs proxy configuration to reach `github.com`. This is applied
automatically when the Spring profile `local` is active, using the default proxy settings, and can be overridden:

- `ch.admin.bit.jeap.initializer.config.jgit.proxy.host` (default: `proxy-bvcol.admin.ch`)
- `ch.admin.bit.jeap.initializer.config.jgit.proxy.port` (default: `8080`)

See `ch.admin.bit.jeap.initializer.config.JGitProxyConfiguration`.

## Security

`WebSecurityConfig` permits unauthenticated access to `/`, `/wizard/**`, `/api/**`, `/style.css` and `/error`, and
disables CSRF protection; all other requests require authentication. The application excludes Spring Boot's
`UserDetailsServiceAutoConfiguration`, so no default in-memory user is created — deploy behind whatever access
control (e.g. a reverse proxy or gateway) is appropriate for the environment, since the API itself does not
enforce authentication on its own endpoints.

## See also

- [Getting Started](getting-started.md) — minimal setup to run the initializer
- [Architecture](architecture.md) — how configured templates are used during generation
- [Template Authoring](template-authoring.md) — the `initializer.yaml` metadata format
