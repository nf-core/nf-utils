package nfcore.utils.functions

import nextflow.Nextflow
import nextflow.Session
import org.pf4j.ExtensionPoint
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

/**
 * Functions for schema validation and parsing
 */
class NfSchema implements ExtensionPoint {

    /**
     * Parse a schema file
     *
     * @param schemaFile Path to the schema JSON file
     * @return Map representing the parsed schema
     */
    static def parseSchema(schemaFile) {
        try {
            return new JsonSlurper().parse(schemaFile.toFile())
        } catch (Exception e) {
            Nextflow.logger.warn("Could not parse schema file: ${e.message}")
            return [:]
        }
    }

    /**
     * Parse a YAML file using a schema
     *
     * @param yamlFile Path to the YAML file
     * @param schemaFile Path to the schema JSON file
     * @return List of objects from the YAML file
     */
    static def parseYaml(yamlFile, schemaFile) {
        // This is a simplified implementation - the actual implementation
        // would use a YAML parser and schema validation
        
        // In a real implementation, this would parse the YAML file 
        // and validate it against the schema, returning the parsed data
        // For now, we'll return a placeholder implementation
        
        try {
            // Delegate to nf-schema plugin if available
            try {
                def samplesheetToList = Class.forName('nfcore.schema.SamplesheetFunctions')
                    .getMethod('samplesheetToList', Object.class, Object.class)
                return samplesheetToList.invoke(null, yamlFile, schemaFile)
            } catch (ClassNotFoundException e) {
                // If nf-schema is not available, log a warning
                Nextflow.logger.warn("nf-schema plugin not available, schema validation will be skipped")
                
                // Use simple YAML parsing without validation
                def yaml = new org.yaml.snakeyaml.Yaml()
                return yaml.load(yamlFile.toFile().text)
            }
        } catch (Exception e) {
            Nextflow.logger.warn("Could not parse YAML file: ${e.message}")
            return []
        }
    }
    
    /**
     * Get default values from a schema
     *
     * @param schemaFile Path to the schema JSON file
     * @return Map of parameters with their default values
     */
    static def getSchemaDefaults(schemaFile) {
        def schema = parseSchema(schemaFile)
        def defaults = [:]
        
        // Process schema properties to extract defaults
        schema.properties?.each { propName, propSchema ->
            if (propSchema.containsKey('default')) {
                defaults[propName] = propSchema.default
            }
        }
        
        return defaults
    }
    
    /**
     * Apply schema defaults to a parameter map
     *
     * @param params Parameter map to update
     * @param schemaFile Path to the schema JSON file
     * @return Updated parameter map with defaults applied
     */
    static def applySchemaDefaults(params, schemaFile) {
        def defaults = getSchemaDefaults(schemaFile)
        
        // Apply defaults to params where not already set
        defaults.each { paramName, defaultValue ->
            if (!params.containsKey(paramName)) {
                params[paramName] = defaultValue
            }
        }
        
        return params
    }
} 