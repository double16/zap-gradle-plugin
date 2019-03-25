package com.patdouble.gradle.zap

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.zaproxy.clientapi.core.ClientApi

/**
 * Executes a ZAP AJAX spider against the applicationUrl. This task will wait until the scan is
 * complete before returning.
 */
@Slf4j
class ZapAjaxSpider extends DefaultTask implements ZapTaskHelper {
    @SuppressWarnings('LineLength')
    ZapAjaxSpider() {
        group = ZapPlugin.GROUP
        description = 'Runs the ZAP AJAX spider against zapConfig.applicationUrl.'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void activeScan() {
        ClientApi zap = project.zapConfig.api()

        zap.ajaxSpider.setOptionBrowserId('htmlunit')
        zap.ajaxSpider.setOptionNumberOfBrowsers(1)
        ProgressLogger progress = createProgressLogger()
        progress.start("AJAX Spidering ${project.zapConfig.applicationUrl}", 'initializing')
        zap.ajaxSpider.scan(project.zapConfig.applicationUrl, 'true', project.name, 'false').value
        waitForCompletion(progress, project.zapConfig.activeScanTimeout as int,
            {
                switch (zap.ajaxSpider.status().value as String) {
                    case 'running':
                        return 50
                    case 'stopped':
                        return 100
                }
            },
            { zap.ajaxSpider.fullResults().valuesMap.size() as String }
        )

        logger.info "AJAX Spider results ${zap.ajaxSpider.fullResults().toString(0)}"
    }

}
