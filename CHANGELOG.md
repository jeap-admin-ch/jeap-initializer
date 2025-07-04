# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

