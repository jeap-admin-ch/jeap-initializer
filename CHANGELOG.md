# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.33.0] - 2025-12-08

### Changed

- Update parent from 30.1.0 to 30.2.0

## [2.32.0] - 2025-12-05

### Changed

- Update parent from 30.0.0 to 30.1.0

## [2.31.0] - 2025-12-03

### Changed

- Update parent from 29.4.0 to 30.0.0

## [2.30.0] - 2025-12-02

### Changed

- Update parent from 29.2.0 to 29.4.0

## [2.29.0] - 2025-11-28

### Changed

- Update parent from 28.3.0 to 29.2.0

## [2.28.0] - 2025-11-14

### Changed

- Update parent from 27.4.0 to 28.3.0

## [2.27.0] - 2025-10-03

### Changed

- Update parent from 27.3.0 to 27.4.0

## [2.26.0] - 2025-09-29

### Changed

- Update parent from 27.2.0 to 27.3.0

## [2.25.0] - 2025-09-19

### Changed

- Update parent from 27.1.1 to 27.2.0

## [2.24.0] - 2025-09-18

### Changed

- Adapted to Dockerfile approach

## [2.23.0] - 2025-09-11

### Changed

- Update parent from 26.76.0 to 27.1.1

## [2.22.0] - 2025-09-02

### Changed

- Update parent from 26.75.1 to 26.76.0

## [2.21.0] - 2025-08-29

### Changed

- Update parent from 26.74.0 to 26.75.1

## [2.20.0] - 2025-08-26

### Changed

- Update parent from 26.72.0 to 26.74.0

## [2.19.2] - 2025-08-15

### Changed

- Sort templates by name for ui

## [2.19.1] - 2025-08-14

### Changed

- Added GitHub deployment file to ArtifactIdContributor

## [2.19.0] - 2025-08-13

### Changed

- Added contributer to rename template files.

## [2.18.1] - 2025-08-05

### Changed

- Fixed an IO Exception being thrown when the target package of a request was a subpackage of the template package.

## [2.18.0] - 2025-08-05

### Changed

- Update parent from 26.71.1 to 26.72.0

## [2.17.0] - 2025-08-05

### Changed

- Assigned a very high priority to the ParameterReplacementContributor.
- Updated parent from 26.71.0 to 26.71.1 

## [2.16.0] - 2025-07-25

### Changed

- Update parent from 26.68.0 to 26.71.0

## [2.15.1] - 2025-07-24

### Changed

- Added dependencyManagement in parent

## [2.15.0] - 2025-07-24

### Changed

- Improved template selection layout
- Update parent from 26.67.0 to 26.68.2
- Show optional module selection only if at least one optional module is defined in the template

## [2.14.0] - 2025-07-04

### Changed

- Update parent from 26.61.0 to 26.67.0

## [2.13.0] - 2025-06-17

### Changed

- Update parent from 26.57.0 to 26.61.0

## [2.12.0] - 2025-06-13

### Changed

- Update parent from 26.55.0 to 26.57.0

## [2.11.0] - 2025-06-06

### Changed

- Update parent from 26.43.2 to 26.55.0

## [2.10.0] - 2025-04-15

### Changed

- Update parent from 26.42.0 to 26.43.2

## [2.9.0] - 2025-04-01

### Changed

- Update parent from 26.33.0 to 26.42.0

## [2.8.0] - 2025-03-06

### Changed

- Update parent from 26.24.2 to 26.33.0

## [2.7.0] - 2025-02-13

### Changed

- Update parent from 26.23.0 to 26.24.2

## [2.6.1] - 2025-02-11

### Changed

- Make test more robust to run on different platforms

## [2.6.0] - 2025-02-10

### Changed

- Update parent from 26.22.3 to 26.23.0
- Publish to maven central

## [2.5.0] - 2025-02-03

### Changed

- Prepare repository for Open Source distribution

## [2.4.1] - 2025-01-09

### Changed

- Add ts-Files to sourceFilesPattern
- Update parent from 26.21.1 to 26.22.2

## [2.4.0] - 2024-12-19

### Changed

- Update parent from 26.19.0 to 26.21.1

## [2.3.2] - 2024-12-11

### Added

- Add .conf files as Sourcefile
- Update parent to 26.19.0

## [2.3.1] - 2024-11-27

### Added

- Add 'Dockerfile' as Sourcefile 

## [2.3.0] - 2024-11-26

### Changed

- Avoid use of http sessions in initializer frontend

## [2.2.0] - 2024-11-22

### Added

- Support for parameter replacement in template files

### Changed

- Generate .tar.gz file instead of .zip file to preserve file permissions

## [2.1.0] - 2024-11-19

### Added

- Support for optional modules
  - Removal of module-specific files and blocks in files
  - Module-specific parameters
  - Module selection and parametrization in the UI

## [2.0.0] - 2024-11-15

### Changed

- Load template metadata and parameter information from initializer.yaml in template repository

## [1.4.0] - 2024-09-25

### Added

- ApplicationNameContributor
- PropertyFilesContributor

### Changed
- Upgraded to jeap-spring-boot-parent 26.3.1

## [1.3.0] - 2024-09-18

### Changed

- UI Module Support
- An empty local Git repository will be initialized
- Upgraded to jeap-spring-boot-parent 26.2.1 

## [1.2.0] - 2024-09-13

### Changed

- Added new UI to generate projects
- Added new field 'id' to TemplateParameter

## [1.1.0] - 2024-09-11

### Changed

- Added option to check out a different GitOps repository under /gitops if configured
- Added 'System' as new parameter for ProjectRequests

## [1.0.0] - 2024-09-06

### Changed

- Initial release

