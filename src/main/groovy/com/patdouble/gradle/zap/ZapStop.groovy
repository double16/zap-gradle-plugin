package com.patdouble.gradle.zap

import groovy.transform.CompileDynamic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Stops the ZAP server gracefully.
 */
@CompileDynamic
class ZapStop extends DefaultTask {

    @SuppressWarnings('LineLength')
    ZapStop() {
        group = ZapPlugin.GROUP
        description = 'Stops the ZAP server ONLY if it has been started during this gradle process. Otherwise does nothing.'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void stopZap() {
        if (project.zapConfig.zapProc != null) {
            project.zapConfig.api().core.shutdown()
            // Kill the process after waiting. The Process API doesn't expose kill directly.
            project.zapConfig.zapProc.waitForOrKill(5000)
        }
    }

}
