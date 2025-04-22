package nfcore.utils.functions

import nextflow.Nextflow
import nextflow.Session
import org.pf4j.ExtensionPoint
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/**
 * Functions for validating nf-core pipeline configurations
 */
class NfValidation implements ExtensionPoint {

    /**
     * Validate parameters against a schema
     *
     * @param params Parameters map
     * @param schemaFile Path to schema JSON file
     * @param ignoreList List of parameters to ignore
     * @param mode Validation mode: 'strict', 'lenient', or 'none'
     * @return Map with validation results
     */
    static def validateParameters(params, schemaFile, ignoreList = [], mode = 'strict') {
        if (mode == 'none') {
            return [
                valid: true,
                params: params,
                errors: []
            ]
        }

        // Get config from session if it exists
        def session = Nextflow.getSession()
        def pluginConfig = session.config.navigate('nfutils')
        
        // Override with plugin config if available
        def actualMode = pluginConfig?.validationMode ?: mode
        def actualIgnoreList = (pluginConfig?.schemaIgnore ?: []) + ignoreList
        
        // Use actual validation logic here - this is simplified
        def schema = new JsonSlurper().parse(schemaFile.toFile())
        def errors = []
        
        // Perform basic validation (simplified example)
        schema.properties.each { propName, propSchema ->
            // Skip parameters in ignore list
            if (propName in actualIgnoreList) {
                return
            }
            
            // Check required parameters
            if (propSchema.required && !(propName in params)) {
                errors << "Required parameter '$propName' is missing"
            }
            
            // Check parameter types
            if (propName in params && params[propName] != null) {
                def value = params[propName]
                switch (propSchema.type) {
                    case 'string':
                        if (!(value instanceof String)) {
                            errors << "Parameter '$propName' should be a string, but got ${value.class.simpleName}"
                        }
                        break
                    case 'number':
                    case 'integer':
                        if (!(value instanceof Number)) {
                            errors << "Parameter '$propName' should be a number, but got ${value.class.simpleName}"
                        }
                        break
                    case 'boolean':
                        if (!(value instanceof Boolean)) {
                            errors << "Parameter '$propName' should be a boolean, but got ${value.class.simpleName}"
                        }
                        break
                }
            }
        }
        
        // In lenient mode, just report errors without failing
        def valid = errors.empty || actualMode == 'lenient'
        
        return [
            valid: valid,
            params: params,
            errors: errors,
            mode: actualMode
        ]
    }
} 