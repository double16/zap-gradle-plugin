/**
 * Copyright (c) 2018-2019, Patrick Double. All right reserved.
 *
 * Released under BSD-3 style license.
 * See http://opensource.org/licenses/BSD-3-Clause
 */
package com.patdouble.gradle.zap

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class ZapPlugin implements Plugin<Project> {

    final static String GROUP = 'verification'
    static final String TASK_ZAP_START = 'zapStart'
    static final String TASK_ZAP_STOP = 'zapStop'
    static final String TASK_ZAP_DOWNLOAD = 'zapDownload'
    static final String TASK_ZAP_ACTIVE_SCAN = 'zapActiveScan'
    static final String TASK_ZAP_REPORT = 'zapReport'
    static final String TASK_ZAP_SPIDER = 'zapSpider'
    static final String TASK_ZAP_AJAX_SPIDER = 'zapAjaxSpider'
    static final String TASK_ZAP_INFO = 'zapInfo'

    void apply(Project target) {
        ZapPluginExtension zapConfig = target.extensions.create('zapConfig', ZapPluginExtension)

        Provider<String> zapInstallDir = zapConfig.version.map { "${zapConfig.zapDir.get()}/ZAP_${it}" }
        target.tasks.create(TASK_ZAP_DOWNLOAD, Download) {
            Provider<String> version = zapConfig.version
            Provider<CharSequence> downloadUrl = version.map { versionStr ->
                if (versionStr.startsWith('w')) { // weekly
                    "https://github.com/zaproxy/zaproxy/releases/download/${versionStr}/ZAP_WEEKLY_D-${versionStr.substring(1)}.zip"
                } else if (versionStr =~ /1[.].*/ || versionStr =~ /2[.][0-7][.].*/) { // release
                    "https://github.com/zaproxy/zaproxy/releases/download/${versionStr}/ZAP_${versionStr}_Crossplatform.zip"
                } else { // recent release
                    "https://github.com/zaproxy/zaproxy/releases/download/v${versionStr}/ZAP_${versionStr}_Crossplatform.zip"
                }
            }
            Provider<RegularFile> destinationFile = version.map { versionStr ->
                if (versionStr.startsWith('w')) { // weekly
                    target.layout.projectDirectory.file("${target.gradle.gradleUserHomeDir}/zap/ZAP_WEEKLY_D-${versionStr.substring(1)}.zip")
                } else { // release
                    target.layout.projectDirectory.file("${target.gradle.gradleUserHomeDir}/zap/ZAP_${versionStr}_Crossplatform.zip")
                }
            }

            outputs.dir zapConfig.zapDir
            onlyIf { !zapConfig.zapInstallDir.isPresent() && !new File(zapInstallDir.get()).exists() }

            group = GROUP
            description = 'Download ZAP'
            src { downloadUrl.get() }
            dest { destinationFile.get().asFile }

            doLast {
                target.copy {
                    from target.zipTree(destinationFile.get().getAsFile())
                    into zapConfig.zapDir
                }
            }
        }

        target.tasks.create(TASK_ZAP_START, ZapStart) {
            finalizedBy TASK_ZAP_STOP
            dependsOn TASK_ZAP_DOWNLOAD
            doFirst {
                if (!zapConfig.zapInstallDir.isPresent()) {
                    if (zapConfig.version.get().startsWith('w')) { // weekly
                        zapConfig.zapInstallDir.set("${zapConfig.zapDir.get()}/ZAP_D-${zapConfig.version.get() - 'w'}")
                    } else {
                        zapConfig.zapInstallDir.set("${zapConfig.zapDir.get()}/ZAP_${zapConfig.version.get()}")
                    }
                }
            }
        }

        target.tasks.create(TASK_ZAP_STOP, ZapStop) {
            mustRunAfter target.tasks.zapStart
            mustRunAfter TASK_ZAP_ACTIVE_SCAN
            mustRunAfter TASK_ZAP_REPORT
        }

        target.tasks.create(TASK_ZAP_SPIDER, ZapSpider) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }

        target.tasks.create(TASK_ZAP_AJAX_SPIDER, ZapAjaxSpider) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }

        target.tasks.create(TASK_ZAP_ACTIVE_SCAN, ZapActiveScan) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
            mustRunAfter target.tasks.zapSpider, target.tasks.zapAjaxSpider
        }

        target.tasks.create(TASK_ZAP_REPORT, ZapReport) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
            mustRunAfter target.tasks.zapSpider, target.tasks.zapAjaxSpider, target.tasks.zapActiveScan
        }

        target.tasks.create(TASK_ZAP_INFO, ZapInfo) {
            dependsOn target.tasks.zapStart
            finalizedBy target.tasks.zapStop
        }
    }

}
