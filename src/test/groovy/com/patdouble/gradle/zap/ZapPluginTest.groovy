package com.patdouble.gradle.zap

import fi.linuxbox.gradle.download.Download

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class ZapPluginTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    void testApplyPluginByClass() {
        project.apply plugin: ZapPlugin
        verifyPlugin()
    }

    @Test
    void testApplyPluginByName() {
        project.apply plugin: 'com.patdouble.zap'
        verifyPlugin()
    }


    private verifyPlugin() {
        assertTrue(project.tasks.zapDownload instanceof Download)
        assertTrue(project.tasks.zapStart instanceof ZapStart)
        assertTrue(project.tasks.zapStart.finalizedBy.mutableValues.contains('zapStop'))
        assertTrue(project.tasks.zapActiveScan instanceof ZapActiveScan)
        assertTrue(project.tasks.zapActiveScan.dependsOn.contains(project.tasks.zapStart))
        assertTrue(project.tasks.zapActiveScan.finalizedBy.mutableValues.contains(project.tasks.zapStop))
        assertTrue(project.tasks.zapReport instanceof ZapReport)
        assertTrue(project.tasks.zapReport.dependsOn.contains(project.tasks.zapStart))
        assertTrue(project.tasks.zapReport.finalizedBy.mutableValues.contains(project.tasks.zapStop))
        assertTrue(project.tasks.zapStop instanceof ZapStop)
        assertTrue(project.tasks.zapStop.mustRunAfter.mutableValues.contains(project.tasks.zapStart))
        assertTrue(project.tasks.zapStop.mustRunAfter.mutableValues.contains('zapActiveScan'))
        assertTrue(project.tasks.zapStop.mustRunAfter.mutableValues.contains('zapReport'))
        assertTrue(project.zapConfig instanceof ZapPluginExtension)
    }

}
