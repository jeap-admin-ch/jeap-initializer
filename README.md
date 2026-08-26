# jEAP Initializer

jEAP Initializer generates ready-to-use project codebases for bootstrapping new projects. Unlike other
initializer-type tools, it generates code from existing projects (called templates) hosted in Git repositories,
rather than baking template content into the tool itself. This means:

* Templates can evolve independently of the initializer
* Template functionality can be verified through its own tests and pipelines
* Additional templates can be added purely via configuration, without changing this application

It ships as a Spring Boot application exposing a REST API and a wizard UI; see
[Getting started](docs/getting-started.md) for how to configure a template and generate a project from it.

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic                                                      | File                                                     |
|--------------------------------------------------------------|-------------------------------------------------------------|
| Getting started (configure a template, generate a project) | [docs/getting-started.md](docs/getting-started.md)         |
| Architecture (generation flow, contributor chain)           | [docs/architecture.md](docs/architecture.md)               |
| Configuration reference (`jeap.initializer.*`)              | [docs/configuration.md](docs/configuration.md)              |
| Template authoring (`initializer.yaml`, markers)             | [docs/template-authoring.md](docs/template-authoring.md)   |
| Extending with custom `ProjectContributor`s                  | [docs/extending.md](docs/extending.md)                     |

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part of the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
