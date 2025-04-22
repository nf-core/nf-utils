package nfcore.utils.config

import nextflow.config.schema.ConfigOption
import nextflow.config.schema.ConfigScope
import nextflow.config.schema.ScopeName
import nextflow.script.dsl.Description
import org.pf4j.ExtensionPoint

@ScopeName('nfutils')
@Description('''
    The `nfutils` scope allows you to configure the `nf-utils` plugin.
''')
class UtilsPluginConfig implements ConfigScope, ExtensionPoint {

    UtilsPluginConfig(Map opts) {
        this.schemaIgnore = opts.schemaIgnore as List ?: []
        this.validationMode = opts.validationMode ?: 'strict'
        this.igenomesBase = opts.igenomesBase ?: null
    }

    @ConfigOption
    @Description('List of parameters to ignore during schema validation.')
    List<String> schemaIgnore

    @ConfigOption
    @Description('Schema validation mode: strict, lenient, or none.')
    String validationMode

    @ConfigOption
    @Description('Path to iGenomes base directory.')
    String igenomesBase
} 