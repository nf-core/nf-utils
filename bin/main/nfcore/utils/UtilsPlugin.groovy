package nfcore.utils

import nextflow.plugin.BasePlugin
import org.pf4j.PluginWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UtilsPlugin extends BasePlugin {
    private static final Logger log = LoggerFactory.getLogger(UtilsPlugin.class)

    UtilsPlugin(PluginWrapper wrapper) {
        super(wrapper)
    }

    @Override
    void start() {
        log.info "nf-utils plugin started"
    }

    @Override
    void stop() {
        log.info "nf-utils plugin stopped"
    }
} 