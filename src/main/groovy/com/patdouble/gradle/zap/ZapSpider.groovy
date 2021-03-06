package com.patdouble.gradle.zap

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.zaproxy.clientapi.core.ClientApi

/**
 * Executes a ZAP spider against the applicationUrl. This task will wait until the scan is complete before returning.
 */
@CompileDynamic
@Slf4j
class ZapSpider extends DefaultTask implements ZapTaskHelper {

    @SuppressWarnings('LineLength')
    ZapSpider() {
        group = ZapPlugin.GROUP
        description = 'Runs the ZAP spider against zapConfig.applicationUrl.'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void activeScan() {
        ClientApi zap = project.zapConfig.api()
        ProgressLogger progress = createProgressLogger()
        progress.start("Spidering ${project.zapConfig.applicationUrl.get()}", 'initializing')

        String scanId = zap.spider.scan(project.zapConfig.applicationUrl.get(), null, 'true', project.name, 'false').value
        logger.debug "Spider scan id = ${scanId}"
        if (scanId) {
            waitForCompletion(progress, project.zapConfig.activeScanTimeout.get() as int,
                    { zap.spider.status(scanId).value as int },
                    { zap.spider.results(scanId).items.size() as String }
            )

            logger.info "Spider results ${zap.spider.results(scanId).toString(0)}"
        } else {
            progress.completed('failed', true)
        }
    }

}
