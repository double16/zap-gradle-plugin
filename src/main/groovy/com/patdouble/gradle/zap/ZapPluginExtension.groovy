package com.patdouble.gradle.zap

import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.zaproxy.clientapi.core.ClientApi

import javax.inject.Inject

class ZapPluginExtension {

    /** The version of ZAP to download, if zapInstallDir is not defined. */
    final Property<String> version

    /** The working directory for ZAP. */
    final Provider<String> zapDir

    /**
     * The directory where ZAP is installed. The directory must contains zap.sh or zap.bat. If not
     * defined, ZAP will be downloaded into the ~/.gradle directory.
     */
    final Property<String> zapInstallDir

    /**
     * The proxy port on which ZAP is listening. When empty, a random port will be chosen. If a port is
     * specified and there is a ZAP process listening on the port, the plugin will use the running instance.
     * In this case, #apiKey will also need to be set.
     */
    final Property<String> proxyPort

    /**
     * Comma and/or space separated list: json, html, xml, md or leave empty for all.
     */
    final Property<String> reportFormat

    /**
     * The name of report files, without extension. The reports will be put into the project buildDir.
     */
    final Property<String> reportOutputPath

    /**
     * The base application URL for spidering.
     */
    final Property<String> applicationUrl

    /**
     * The timeout, in seconds, to wait for the active scan or spidering to complete. The timeout applies
     * to each spider and scan task, it's not cumulative. A value of 0 will not timeout.
     */
    final Property<String> activeScanTimeout

    /**
     * The API key for authenticating to ZAP. Defaults to a random value and is automatically used in
     * tasks and when calling the #api() and #api(Closure) methods. This is required when using an
     * already running ZAP.
     */
    final Property<String> apiKey

    /**
     * Extra parameters to pass on the ZAP command line. Some interesting parameters (list all with zap.sh -help).
     *
     * 	-config <kvpair>         Overrides the specified key=value pair in the configuration file
     * 	-configfile <path>       Overrides the key=value pairs with those in the specified properties file
     * 	-dir <dir>               Uses the specified directory instead of the default one
     * 	-newsession <path>       Creates a new session at the given location
     * 	-session <path>          Opens the given session after starting ZAP
     * 	-addoninstall <addon>    Install the specified add-on from the ZAP Marketplace
     * 	-addoninstallall         Install all available add-ons from the ZAP Marketplace
     * 	-addonuninstall <addon>  Uninstall the specified add-on
     * 	-addonupdate             Update all changed add-ons from the ZAP Marketplace
     * 	-addonlist               List all of the installed add-ons
     */
    ListProperty<String> parameters

    /**
     * ZAP process started by the ZapStart task. May not be populated if the #proxyPort points to an already
     * running ZAP.
     */
    protected Process zapProc

    @Inject
    ZapPluginExtension(Gradle gradle, ObjectFactory objects) {
        version = objects.property(String).convention('2.8.0')
        zapDir = version.map { "${gradle.gradleUserHomeDir}/zap/${it}" }
        zapInstallDir = objects.property(String)
        proxyPort = objects.property(String)
        reportFormat = objects.property(String).convention('json,xml,html,md')
        reportOutputPath = objects.property(String).convention('reports/zap/zapReport')
        applicationUrl = objects.property(String)
        activeScanTimeout = objects.property(String).convention('300')
        apiKey = objects.property(String).convention(UUID.randomUUID() as String)
        parameters = objects.listProperty(String).convention([])
    }

    /**
     * Get an instance of ClienApi with the host, port and API key set.
     */
    ClientApi api() {
        new ClientApi('localhost', proxyPort.get() as int, apiKey.get())
    }

    /**
     * Call a closure with a ClientApi instance as the delegate. This allows things like:
     *
     * zapConfig.api {
     *     spider.setOptionAcceptCookies(true)
     *     ascan.setOptionHandleAntiCSRFTokens(true)
     * }
     */
    void api(Closure closure) {
        closure.delegate = api()
        closure.call()
    }

}
