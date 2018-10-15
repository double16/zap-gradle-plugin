package com.pros.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ZapStop extends DefaultTask {
    @TaskAction
    def stopZap() {
        if (project.zapConfig.zapProc != null)
        {
            // Kill the process after waiting 1ms. The Process API doesn't expose kill directly.
            project.zapConfig.zapProc.waitForOrKill(1)
        }
    }
}