package com.patdouble.gradle.zap

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ClientApi

/**
 * Starts the ZAP daemon. This will persist after the gradle run if stopZap is not called.
 */
@CompileDynamic
@Slf4j
class ZapStart extends DefaultTask implements ZapTaskHelper {

    @SuppressWarnings('LineLength')
    ZapStart() {
        group = ZapPlugin.GROUP
        description = 'Starts the ZAP daemon.'
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void startZap() {
        if (project.zapConfig.zapProc != null) {
            return
        }

        // Check for a running ZAP listening on the port, which is useful for debugging configuration
        if (isZapRunning(project.zapConfig)) {
            return
        }

        String workingDir = project.zapConfig.zapInstallDir.get()
        project.zapConfig.proxyPort.set(resolvePort())

        List<String> command = [workingDir + File.separator + (Os.isFamily(Os.FAMILY_WINDOWS) ? 'zap.bat' : 'zap.sh'),
                                '-daemon', '-port', project.zapConfig.proxyPort.get(), '-config', "api.key=${project.zapConfig.apiKey.get()}" as String]
        command.addAll(project.zapConfig.parameters.get().collect { it as String })
        ProcessBuilder builder = new ProcessBuilder(command)
        File stdout = new File(project.buildDir, "${project.zapConfig.reportOutputPath.get()}.out.log")
        File stderr = new File(project.buildDir, "${project.zapConfig.reportOutputPath.get()}.err.log")
        stdout.parentFile.mkdirs()
        builder.redirectOutput(stdout)
        builder.redirectError(stderr)

        builder.directory(new File(workingDir))
        logger.info "Running ZAP using ${builder.command()} in ${builder.directory()}"
        Thread.start {
            project.zapConfig.zapProc = builder.start()
        }

        ClientApi zap = new ClientApi('localhost',
                project.zapConfig.proxyPort.get() as int,
                project.zapConfig.apiKey.getOrNull() as String)
        zap.waitForSuccessfulConnectionToZap(120)

        zap.core.setMode('protect')
    }

    protected String resolvePort() {
        if (project.zapConfig.proxyPort.isPresent()) {
            return project.zapConfig.proxyPort.get()
        }

        Integer port = null
        ServerSocket socket = null
        try {
            socket = new ServerSocket(0)
            port = socket.getLocalPort()
        } finally {
            socket?.close()
        }
        return port as String
    }

}
