package nfcore.utils.functions

import nextflow.Nextflow
import nextflow.Session
import org.pf4j.ExtensionPoint
import nextflow.Channel
import java.nio.file.Path
import java.nio.file.Paths
import nextflow.extension.FilesEx

/**
 * Functions for handling reference genome data
 */
class References implements ExtensionPoint {

    /**
     * Extract references files from a references YAML
     *
     * @param references Channel of meta/readme tuples from references YAML
     * @param param File parameter from user params (overrides references from YAML)
     * @param attribute Attribute name in the references YAML
     * @param basepath Base path for replacing ${params.igenomes_base} in references
     * @return Channel of meta/file tuples
     */
    static def getReferencesFile(references, param, attribute, basepath) {
        return references
            .map { meta, _readme ->
                if (param || meta[attribute]) {
                    [meta.subMap(['id']), FilesEx.asPath(param ?: meta[attribute].replace('${params.igenomes_base}', basepath))]
                }
                else {
                    null
                }
            }
            .collect()
    }

    /**
     * Extract references values from a references YAML
     *
     * @param references Channel of meta/readme tuples from references YAML
     * @param param Value parameter from user params (overrides references from YAML)
     * @param attribute Attribute name in the references YAML
     * @return Channel of meta/value tuples
     */
    static def getReferencesValue(references, param, attribute) {
        return references
            .map { meta, _readme ->
                if (param || meta[attribute]) {
                    [meta.subMap(['id']), param ?: meta[attribute]]
                }
                else {
                    null
                }
            }
            .collect()
    }

    /**
     * Load a references YAML and convert to a channel of meta/readme tuples
     *
     * @param yamlFile Path to the references YAML file
     * @param schemaFile Path to the schema JSON file (default is plugin resource)
     * @return Channel of meta/readme tuples
     */
    static def loadReferencesYaml(yamlFile, schemaFile = null) {
        def session = Nextflow.getSession()
        def pluginId = 'nf-utils'
        
        // Use the bundled schema if none provided
        def actualSchemaFile = schemaFile ?: 
            session.getPluginResource(pluginId, 'references/schema_references.json')
            
        // Get samplesheetToList function from nf-schema plugin if available
        def samplesheetToList = Class.forName('nfcore.schema.SamplesheetFunctions')
            .getMethod('samplesheetToList', Object.class, Object.class)
            
        // Convert YAML to list using nf-schema's samplesheetToList
        def referencesList = samplesheetToList.invoke(null, yamlFile, actualSchemaFile)
        
        // Create a channel from the list
        return Channel.fromList(referencesList)
    }
} 