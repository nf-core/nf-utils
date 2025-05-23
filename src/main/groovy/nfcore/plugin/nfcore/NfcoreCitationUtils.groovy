/*
 * Copyright 2025, nf-core
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nfcore.plugin.nfcore

import groovy.util.logging.Slf4j
import nextflow.Session
import org.yaml.snakeyaml.Yaml

/**
 * Utility functions for nf-core citations
 */
@Slf4j
class NfcoreCitationUtils {

    /**
     * Generate citation for a tool from meta.yml at the module level
     * @param metaFilePath Path to the meta.yml file (String or File)
     * @return Map containing tool citations for the module
     */
    static Map generateModuleToolCitation(Object metaFilePath) {
        File file = metaFilePath instanceof File ? metaFilePath : new File(metaFilePath.toString())
        if (!file.exists()) {
            throw new IllegalArgumentException("meta.yml file not found at: ${file.getAbsolutePath()}")
        }
        def yaml = new Yaml()
        Map meta
        file.withInputStream { is ->
            meta = yaml.load(is)
        }
        def tools = meta?.tools ?: []
        def moduleCitations = [:]

        tools.each { toolEntry ->
            toolEntry.each { toolName, toolInfo ->
                def citation = toolName
                def bibEntry = null

                // Generate citation text
                if (toolInfo instanceof Map) {
                    if (toolInfo.doi) {
                        citation += " (DOI: ${toolInfo.doi})"
                    } else if (toolInfo.description) {
                        citation += " (${toolInfo.description})"
                    }

                    // Generate bibliography entry
                    def author = toolInfo.author ?: ""
                    def year = toolInfo.year ?: ""
                    def title = toolInfo.title ?: toolName
                    def journal = toolInfo.journal ?: ""
                    def doi = toolInfo.doi ? "doi: ${toolInfo.doi}" : ""
                    def url = toolInfo.homepage ?: ""
                    def bibCitation = [author, year, title, journal, doi].findAll { it }.join(". ")
                    if (url) bibCitation += ". <a href='${url}'>${url}</a>"
                    bibEntry = "<li>${bibCitation}</li>"
                }

                moduleCitations[toolName] = [
                        citation    : citation,
                        bibliography: bibEntry
                ]
            }
        }

        return moduleCitations
    }

    /**
     * Generate methods description for MultiQC using collected citations
     * @param collectedCitations Map containing all tool citations from modules
     * @return Formatted citation string for tools used in the workflow
     */
    static String toolCitationText(Map collectedCitations) {
        if (collectedCitations.isEmpty()) {
            return "No tools used in the workflow."
        }

        def toolCitations = collectedCitations.values().collect { it.citation }
        return "Tools used in the workflow included: " + toolCitations.join(', ') + "."
    }

    /**
     * Generate bibliography text from collected citations
     * @param collectedCitations Map containing all tool citations from modules
     * @return Formatted bibliography HTML for tools used in the workflow
     */
    static String toolBibliographyText(Map collectedCitations) {
        if (collectedCitations.isEmpty()) {
            return "No bibliography entries found."
        }

        def bibEntries = collectedCitations.values()
                .findAll { it.bibliography }
                .collect { it.bibliography }

        return bibEntries.join(" ")
    }

    /**
     * Generate methods description text using collected citations
     * @param mqc_methods_yaml MultiQC methods YAML file
     * @param collectedCitations Map containing all tool citations from modules (optional)
     * @param meta Additional metadata (optional)
     * @return Formatted methods description HTML
     */
    static String methodsDescriptionText(File mqc_methods_yaml, Map collectedCitations = [:], Map meta = [:]) {
        // Convert to a named map so can be used as with familiar NXF ${workflow} variable syntax in the MultiQC YML file
        if (!meta) meta = [:]
        def session = (Session) nextflow.Nextflow.session
        if (!meta.containsKey("workflow")) {
            meta.workflow = session.getWorkflowMetadata()?.toMap() ?: [:]
        }
        if (!meta.containsKey("manifest_map")) {
            meta["manifest_map"] = session.getManifest()?.toMap() ?: [:]
        }
        // Pipeline DOI
        if (meta.manifest_map?.doi) {
            def temp_doi_ref = ""
            def manifest_doi = meta.manifest_map.doi.tokenize(",")
            manifest_doi.each { doi_ref ->
                temp_doi_ref += "(doi: <a href='https://doi.org/${doi_ref.replace('https://doi.org/', '').replace(' ', '')}'>${doi_ref.replace('https://doi.org/', '').replace(' ', '')}</a>), "
            }
            meta["doi_text"] = temp_doi_ref[0..-3]
        } else {
            meta["doi_text"] = ""
        }
        meta["nodoi_text"] = meta.manifest_map?.doi ? "" : "<li>If available, make sure to update the text to include the Zenodo DOI of version of the pipeline used. </li>"
        // Generate tool citations and bibliography if not already provided
        if (!meta.containsKey("tool_citations")) {
            meta["tool_citations"] = toolCitationText(collectedCitations)
        }
        if (!meta.containsKey("tool_bibliography")) {
            meta["tool_bibliography"] = toolBibliographyText(collectedCitations)
        }
        def engine = new groovy.text.SimpleTemplateEngine()
        def description_html = engine.createTemplate(mqc_methods_yaml.text).make(meta)
        return description_html.toString()
    }
} 