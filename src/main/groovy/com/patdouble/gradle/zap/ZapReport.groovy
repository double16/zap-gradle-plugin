package com.patdouble.gradle.zap

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ClientApi

/**
 * Grabs the alert report from the running ZAP instances.
 */
class ZapReport extends DefaultTask {
    @SuppressWarnings('LineLength')
    ZapReport() {
        group = ZapPlugin.GROUP
        description = 'Generates a report with the current ZAP alerts for applicationUrl at reportOutputPath with type remoteFormat (HTML, JSON, or XML)'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void outputReport() {
        ClientApi zap = project.zapConfig.api()
        new File(project.buildDir, project.zapConfig.reportOutputPath.get()).parentFile.mkdirs()
        String reportFormat = project.zapConfig.reportFormat.get()
        reportFormat.toLowerCase().split(/[^a-z]+/).asType(List).unique().each { format ->
            switch (format) {
                case 'json':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath.get()}.json").bytes = zap.core.jsonreport()
                    break
                case 'xml':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath.get()}.xml").bytes = zap.core.xmlreport()
                    break
                case 'html':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath.get()}.html").bytes = zap.core.htmlreport()
                    break
                case 'md':
                    new File(project.buildDir, "${project.zapConfig.reportOutputPath.get()}.md").bytes = zap.core.mdreport()
                    break
                default:
                    throw new GradleException("Unknown report format: ${format}")
            }
        }
    }

}
