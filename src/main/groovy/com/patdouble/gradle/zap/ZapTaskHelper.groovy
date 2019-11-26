package com.patdouble.gradle.zap

import org.gradle.api.GradleException
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.zaproxy.clientapi.core.ClientApi

/**
 * Common methods used in tasks.
 */
trait ZapTaskHelper {

    ProgressLogger createProgressLogger() {
        return ((ProjectInternal) project).services.get(ProgressLoggerFactory).newOperation(this.class)
    }

    /**
     * Wait for a background process in ZAP to complete and report progress to Gradle.
     * @param progress Gradle progress logger
     * @param timeout timeout in seconds
     * @param fetchStatus closure returning a percent complete, 0-100 with 100 meaning complete
     * @param progressMessage optional closure returning a message to include in the progress logger
     * (the percent complete is already included)
     */
    void waitForCompletion(ProgressLogger progress, int timeout,
                           Closure<Integer> fetchStatus,
                           Closure<String> progressMessage = null) {
        long end = (timeout > 0) ? System.currentTimeMillis() + timeout * 1000 : Long.MAX_VALUE
        int status
        while ((status = fetchStatus.call()) < 100) {
            if (System.currentTimeMillis() > end) {
                String msg = "operation did not complete within ${timeout} seconds"
                progress?.completed(msg, true)
                throw new GradleException(msg)
            }
            String msg = (progressMessage?.call()) ?: ''
            progress?.progress("${status}%, ${msg}")
            Thread.sleep(2000)
        }
        progress?.completed('100%', false)
    }

    /**
     * Check if the ZAP process defined in the extension is listening on the proxy port.
     */
    boolean isZapRunning(ZapPluginExtension zapConfig) {
        if (zapConfig.proxyPort.isPresent()) {
            try {
                new ClientApi(
                        'localhost',
                        zapConfig.proxyPort.get() as int,
                        zapConfig.apiKey.getOrNull() as String).core.version()
                logger.info "ZAP running on port ${zapConfig.proxyPort.get()}"
                return true
            } catch (IOException e) {
                logger.debug "ZAP not running on port ${zapConfig.proxyPort.get()}", e
            }
        }
        return false
    }

}
