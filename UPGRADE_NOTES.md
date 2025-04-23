# OpenSearch Late Interaction Plugin Upgrade Notes

## Changes Made for OpenSearch 2.15.0+ Compatibility

### 1. Build Configuration Updates
- Updated `build.gradle` to target OpenSearch 2.15.0 (from 2.12.0)
- Updated `plugin-descriptor.properties` to specify compatibility with 2.15.0

### 2. API Compatibility Changes

#### Package Import Updates
- Updated import paths from `org.opensearch.common.xcontent.*` to `org.opensearch.xcontent.*`
- Updated import paths from `org.opensearch.common.io.stream.*` to `org.opensearch.core.common.io.stream.*`
- Updated import path for ParseField from `org.opensearch.common.ParseField` to `org.opensearch.core.ParseField`

#### LateInteractionPlugin.java Changes
- Updated the `getRescorers()` method return type from `List<RescorerBuilder<?>>` to `List<RescorerSpec<?>>`
- Implemented the new RescorerSpec factory pattern using `RescorerSpec.of()`

#### MaxSimRescorerBuilder.java Changes
- Added the required `innerBuildContext()` method to replace the previous `build()` method
- Created a new inner class `MaxSimRescoreContext` that extends `RescoreContext` to hold the query state
- Updated method signature to use `QueryShardContext` instead of `SearchExecutionContext`
- Added `getQueryWeight()` method to `MaxSimRescoreContext` to support rescoring weight configuration

#### MaxSimRescorer.java Changes
- Refactored to use a singleton pattern with `INSTANCE` static field
- Removed instance fields and constructor in favor of retrieving parameters from the context
- Updated `rescore()` and `explain()` methods to use the new `MaxSimRescoreContext`
- Modified weight handling to check if the context is an instance of `MaxSimRescoreContext` before retrieving the weight

### 3. Integration Testing
- Added GitHub Actions workflow for automated testing with OpenSearch 2.15.0 and 2.17.0
- Created `integration-test.yml` to build, install, and validate the plugin in Docker containers

### 4. AWS OpenSearch Service Deployment
- Added documentation for deploying the plugin to AWS OpenSearch Service
- Updated README.md with compatibility information and AWS deployment instructions

## Known Issues and Limitations
- The current implementation uses placeholder document vectors for demonstration purposes
- In a production-ready version, vectors would be retrieved from the document field

## Next Steps
- Implement actual vector retrieval from indexed documents
- Add comprehensive tests for various OpenSearch versions
- Explore advanced optimizations for token vectors in OpenSearch