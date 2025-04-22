package nfcore.utils.functions

import nextflow.Nextflow
import nextflow.Session
import org.pf4j.ExtensionPoint
import groovy.json.JsonSlurper
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

/**
 * Functions for Nextflow pipeline utilities
 */
class NextflowPipeline implements ExtensionPoint {

    /**
     * Check if software versions are defined and load them
     * 
     * @param modulesVersion Path to the modules versions file (default: conf/modules.config)
     * @param bumpVersion Whether to update the version for cache invalidation
     * @return Map of module versions 
     */
    static def checkVersions(modulesVersion = null, bumpVersion = false) {
        def session = Nextflow.getSession()
        def workDir = session.workDir
        def configVersion = modulesVersion ?: Paths.get(session.config.navigate('projectDir'), 'conf/modules.config')

        // Check if versions file exists
        def versionsFile = workDir.resolve('.versions.json')
        def versions = [:]
        
        if (Files.exists(versionsFile)) {
            try {
                versions = new JsonSlurper().parse(versionsFile.toFile())
            } catch (Exception e) {
                Nextflow.logger.warn("Could not parse versions file: ${e.message}")
            }
        }
        
        // Parse modules config if it exists
        if (Files.exists(configVersion)) {
            try {
                def content = configVersion.text
                def matcher = content =~ /process\s*\{\s*withName:\s*'?([^'{\s]+)'?[^}]*version\s*=\s*['"]([^'"]+)['"]/
                
                while (matcher.find()) {
                    def moduleName = matcher.group(1)
                    def version = matcher.group(2)
                    
                    // Bump version if specified
                    if (bumpVersion && version) {
                        if (version.contains('.')) {
                            def parts = version.tokenize('.')
                            def last = parts.removeLast()
                            if (last.isInteger()) {
                                last = (last as Integer) + 1
                                version = (parts + [last]).join('.')
                            }
                        } else if (version.isInteger()) {
                            version = (version as Integer) + 1 as String
                        }
                    }
                    
                    versions[moduleName] = version
                }
                
                // Write versions to file
                versionsFile.text = groovy.json.JsonOutput.toJson(versions)
            } catch (Exception e) {
                Nextflow.logger.warn("Could not parse modules config: ${e.message}")
            }
        }
        
        return versions
    }
    
    /**
     * Check memory resource
     * 
     * @param requested_memory Memory string like "1.GB" or "1000.MB"
     * @param available_memory Available memory as string (default: totalSystemMemory)
     * @param multi Multiplier for available memory (default: 0.9)
     * @return Corrected memory string 
     */
    static def checkMemoryResource(requested_memory, available_memory = null, multi = 0.9) {
        // Get available memory from system if not provided
        def system_memory = Runtime.getRuntime().totalMemory()
        def available = available_memory ? 
            convertMemoryToBytes(available_memory) : 
            (system_memory * multi)
            
        // Convert requested memory to bytes
        def requested = convertMemoryToBytes(requested_memory)
        
        // Check if requested memory is less than available
        if (requested > available) {
            def avail_gb = available / (1024 * 1024 * 1024)
            return "${avail_gb.round(2)}.GB"
        }
        
        return requested_memory
    }
    
    /**
     * Convert memory string to bytes
     * 
     * @param memory Memory string like "1.GB" or "1000.MB"
     * @return Memory in bytes
     */
    private static def convertMemoryToBytes(memory) {
        def pattern = /(\d+(?:\.\d+)?)\.?([KMGT]B)?/
        def matcher = (memory =~ pattern)
        
        if (matcher.matches()) {
            def value = matcher.group(1) as double
            def unit = matcher.group(2)
            
            switch (unit) {
                case 'KB':
                    return value * 1024
                case 'MB':
                    return value * 1024 * 1024
                case 'GB':
                    return value * 1024 * 1024 * 1024
                case 'TB':
                    return value * 1024 * 1024 * 1024 * 1024
                default:
                    return value
            }
        }
        
        return memory
    }
} 