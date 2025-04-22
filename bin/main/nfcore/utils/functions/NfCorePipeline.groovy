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
 * Functions for nf-core pipeline utilities
 */
class NfCorePipeline implements ExtensionPoint {

    /**
     * Check nf-core pipeline minimum version requirements
     *
     * @param minNfCoreVersion Minimum nf-core version required
     * @param pipelineVersion Current pipeline version 
     * @return Map with validation results
     */
    static def checkNfCoreVersion(minNfCoreVersion, pipelineVersion) {
        // Parse versions
        def minList = minNfCoreVersion.toString().tokenize('.')
        def currList = pipelineVersion.toString().tokenize('.')
        
        // Compare major versions
        if (minList[0].toInteger() > currList[0].toInteger()) {
            return [
                valid: false,
                message: "This pipeline needs nf-core version $minNfCoreVersion or greater - you are running version $pipelineVersion"
            ]
        }
        
        // Compare minor versions if major are equal
        if (minList[0].toInteger() == currList[0].toInteger() && 
            minList.size() > 1 && currList.size() > 1 && 
            minList[1].toInteger() > currList[1].toInteger()) {
            return [
                valid: false,
                message: "This pipeline needs nf-core version $minNfCoreVersion or greater - you are running version $pipelineVersion"
            ]
        }
        
        return [
            valid: true,
            message: "nf-core version $pipelineVersion meets the minimum requirement ($minNfCoreVersion)"
        ]
    }
    
    /**
     * Get pipeline metadata from nextflow.config
     *
     * @param configPath Path to nextflow.config file (default: from current project)
     * @return Map with pipeline metadata 
     */
    static def getPipelineMetadata(configPath = null) {
        def session = Nextflow.getSession()
        def projectDir = session.config.navigate('projectDir')
        def actualConfigPath = configPath ?: Paths.get(projectDir.toString(), 'nextflow.config')
        
        if (!Files.exists(actualConfigPath)) {
            return [:]
        }
        
        def metadata = [:]
        try {
            def configText = actualConfigPath.text
            
            // Extract manifest section
            def manifestPattern = /manifest\s*\{([^}]+)\}/
            def manifestMatcher = configText =~ manifestPattern
            if (manifestMatcher.find()) {
                def manifestBlock = manifestMatcher.group(1)
                
                // Extract key-value pairs
                def keyValuePattern = /(\w+)\s*=\s*['"]([^'"]+)['"]/
                def keyValueMatcher = manifestBlock =~ keyValuePattern
                while (keyValueMatcher.find()) {
                    metadata[keyValueMatcher.group(1)] = keyValueMatcher.group(2)
                }
            }
            
            // Extract params section for pipeline name and version if not found in manifest
            if (!metadata.name || !metadata.version) {
                def paramsPattern = /params\s*\{([^}]+)\}/
                def paramsMatcher = configText =~ paramsPattern
                if (paramsMatcher.find()) {
                    def paramsBlock = paramsMatcher.group(1)
                    
                    // Extract key-value pairs
                    def keyValuePattern = /(\w+)\s*=\s*['"]([^'"]+)['"]/
                    def keyValueMatcher = paramsBlock =~ keyValuePattern
                    while (keyValueMatcher.find()) {
                        if (keyValueMatcher.group(1) == 'pipeline_name' && !metadata.name) {
                            metadata.name = keyValueMatcher.group(2)
                        }
                        if (keyValueMatcher.group(1) == 'pipeline_version' && !metadata.version) {
                            metadata.version = keyValueMatcher.group(2)
                        }
                    }
                }
            }
        } catch (Exception e) {
            Nextflow.logger.warn("Could not parse nextflow.config: ${e.message}")
        }
        
        return metadata
    }
    
    /**
     * Check if this is an nf-core pipeline
     * 
     * @return boolean indicating if this is an nf-core pipeline
     */
    static boolean isNfCorePipeline() {
        def metadata = getPipelineMetadata()
        return metadata.name?.startsWith('nf-core/') || 
               metadata.homePage?.contains('nf-core') ||
               metadata.description?.contains('nf-core')
    }
} 