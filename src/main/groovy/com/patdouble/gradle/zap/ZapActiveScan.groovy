package com.patdouble.gradle.zap

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLogger
import org.zaproxy.clientapi.core.ClientApi

/**
 * Executes a ZAP active scan against the applicationUrl. This task will wait until the scan is
 * complete before returning.
 */
@Slf4j
class ZapActiveScan extends DefaultTask implements ZapTaskHelper {
    @SuppressWarnings('LineLength')
    ZapActiveScan() {
        group = ZapPlugin.GROUP
        description = 'Runs the ZAP active scanner against zapConfig.applicationUrl. It is recommended that this be done after any automated tests have completed so that the proxy is aware of those URLs.'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void activeScan() {
        ClientApi zap = project.zapConfig.api()
        ProgressLogger progress = createProgressLogger()
        progress.start("Active scan ${project.zapConfig.applicationUrl.get()}", 'initializing')

        zap.accessUrl(project.zapConfig.applicationUrl.get())
        String scanId = zap.ascan.scan(project.zapConfig.applicationUrl.get(), 'true', '', '', '', '').value
        logger.debug "Active scan id = ${scanId}"
        if (scanId) {
            waitForCompletion(progress, project.zapConfig.activeScanTimeout.get() as int,
                    { zap.ascan.status(scanId).value as int },
                    {
                    zap.ascan.scans().items.findAll { it.getStringValue('id') == scanId }.collect {
                        "${it.getStringValue('reqCount')} requests, ${it.getStringValue('alertCount')} alerts"
                    }.find()
                }
            )
        } else {
            progress.completed('failed', true)
        }
    }

}
