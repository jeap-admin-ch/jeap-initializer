# Template Authoring

A "template" is a regular Git repository containing a project skeleton, plus one metadata file,
`initializer.yaml`, at its root. This page describes how to write that metadata and how to mark
optional/parameterized content in the template's source files so the initializer's contributors
(see [Architecture](architecture.md)) can process it correctly.

## `initializer.yaml`

```yaml
name: "jEAP Example Template"
description: "Provides an instance of a jEAP example app"
# These values describe the template itself; they are replaced with values from the generation
# request during project generation (see the contributor table in Architecture)
base-package: ch.admin.bit.jme
system-name: jme
platform: my-cloud-platform
artifact-id: jme-example-app
group-id: ch.admin.bit.jme

# Parameters that must/can be provided when generating a project from this template
template-parameters:
  - id: awsAccountId
    name: AWS account id
    description: AWS Development Environment Account ID

# Optional modules that can be selected when generating a project from this template
template-modules:
  - id: object-storage
    name: Object Storage (S3)
    description: Provides dependencies and configuration for integrating with an AWS S3 object storage
    module-parameters:
      - id: bucket-name
        name: Bucket Name
        description: Name of the bucket used to store/read data
```

`name`, `base-package`, `system-name`, `artifact-id`, `group-id` and `platform` are **required** —
`CachingTemplateRepository` asserts their presence when loading the template and fails the request otherwise.
`platform` must reference a platform key configured under `jeap.initializer.platforms` (see
[Configuration](configuration.md)).

The file is parsed as YAML with kebab-case property names mapped onto `ProjectTemplate` /
`TemplateParameter` / `TemplateModule`. `TemplateFileRemovalContributor` deletes `initializer.yaml` from the
generated output, so it never leaks into generated projects.

## Marking optional module content

Content that should only be included when a specific optional module is selected can be marked in two ways,
processed by `TemplateModuleContributor`:

**Code blocks** — wrap the block with start/end markers using the module's id:

```
// START MODULE object-storage
... module-specific code ...
// END MODULE object-storage
```

The comment prefix (`//`, `#`, `<!--`, ...) is up to the template; only the `START MODULE <id>` / `END MODULE
<id>` text is matched. If the module is *not* selected, the whole block (including the markers) is removed; if it
*is* selected, only the marker lines are stripped and the content is kept.

**Whole files** — mark a file as belonging to a module by including the text `MODULE-SPECIFIC FILE FOR MODULE
<id>` anywhere in it (e.g. as a leading comment). If the module is not selected, the file is deleted entirely.

## Marking content to always remove

Blocks that must never appear in a generated project (e.g. template-only tooling or documentation) use the same
mechanism without a module id, processed by `CodeRemoverContributor`:

```
// START INITIALIZER DELETE
... content only relevant to the template repository itself ...
// END INITIALIZER DELETE
```

## Template parameters in source files

A source file can declare a placeholder to be substituted with a value from the generation request's
`templateParameters` map, processed by `ParameterReplacementContributor`:

```
// INITIALIZER PARAMETER awsAccountId VALUE 123456789012
```

The declaration line itself is removed from the output, and every occurrence of `123456789012` elsewhere in the
same file is replaced with the actual value supplied for `awsAccountId` in the request. This lets a template use
a realistic placeholder value (so the template repository itself remains buildable/testable) while still being
parameterizable at generation time.

## Files needing special renaming

Some file formats (e.g. JSON) cannot carry comment-based markers. For these, ship the file with an
`.initializer-template` suffix (e.g. `package.json.initializer-template`); `TemplateFileRenamingContributor`
renames it to its real name (`package.json`) during generation, overwriting any file that already exists under
that name.

## Fields substituted automatically

Beyond the marker mechanisms above, several template fields declared in `initializer.yaml` are substituted
automatically wherever they appear in specific files — see the contributor table in [Architecture](architecture.md)
for exactly which files each of `name`, `artifact-id`, `group-id`, `system-name` and `base-package` is replaced in
(e.g. Java package folders/imports for `base-package`, `<groupId>` in POMs for `group-id`, and so on).

## See also

- [Architecture](architecture.md) — the contributor chain that processes these markers
- [Configuration](configuration.md) — how templates and platforms are registered
- [Getting Started](getting-started.md) — generating a project from a configured template
