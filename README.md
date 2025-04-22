# nf-utils

Nextflow plugin containing utility functions from nf-core pipelines.

## Installation

Add the plugin to your Nextflow configuration:

```groovy
plugins {
    id 'nf-utils@1.0.0'
}
```

## Configuration

You can configure the plugin in your `nextflow.config` file:

```groovy
nfutils {
    schemaIgnore = ['ignored_param1', 'ignored_param2']
    validationMode = 'strict'  // strict, lenient, or none
    igenomesBase = '/path/to/igenomes'
}
```

## Available Functions

### References

```groovy
import nfcore.utils.functions.References

// Load references from a YAML file
references = References.loadReferencesYaml(params.references)

// Get a references file
fasta = References.getReferencesFile(references, params.fasta, 'fasta', params.igenomes_base)

// Get a references value
species = References.getReferencesValue(references, params.species, 'species')
```

### NfValidation

```groovy
import nfcore.utils.functions.NfValidation

// Validate parameters against a schema
validation = NfValidation.validateParameters(params, file("${projectDir}/nextflow_schema.json"))

// Check if validation passed
if (!validation.valid) {
    // Print errors
    validation.errors.each { 
        log.error it 
    }
    exit 1
}
```

### NextflowPipeline

```groovy
import nfcore.utils.functions.NextflowPipeline

// Check and load module versions
versions = NextflowPipeline.checkVersions()

// Check memory resource
memory = NextflowPipeline.checkMemoryResource("16.GB")
```

### NfCorePipeline

```groovy
import nfcore.utils.functions.NfCorePipeline

// Check if running an nf-core pipeline
if (NfCorePipeline.isNfCorePipeline()) {
    log.info "This is an nf-core pipeline"
}

// Check nf-core version requirements
versionCheck = NfCorePipeline.checkNfCoreVersion("2.0", "2.1.0")
if (!versionCheck.valid) {
    log.error versionCheck.message
}
```

### NfSchema

```groovy
import nfcore.utils.functions.NfSchema

// Parse a schema file
schema = NfSchema.parseSchema(file("${projectDir}/nextflow_schema.json"))

// Apply schema defaults to params
params = NfSchema.applySchemaDefaults(params, file("${projectDir}/nextflow_schema.json"))
```

## Development

### Building the plugin

The plugin uses the nextflow-plugin-gradle Gradle plugin which simplifies plugin development.

```bash
./gradlew assemble
```

This will create the plugin archive in `build/libs` directory.

### Testing the plugin

To test the plugin in development mode:

```bash
export NXF_PLUGINS_MODE=dev
export NXF_PLUGINS_DEV=/path/to/nf-utils
```

Then run your pipeline with the plugin:

```bash
nextflow run main.nf -plugins nf-utils
```

### Publishing the plugin

To publish a new release of the plugin:

```bash
./gradlew releasePlugin
```

This will create a new release on GitHub and update the Nextflow plugins registry.

## License

MIT License
