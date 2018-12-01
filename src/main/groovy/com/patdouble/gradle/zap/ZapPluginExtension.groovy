package com.patdouble.gradle.zap

import org.zaproxy.clientapi.core.ClientApi

class ZapPluginExtension {
    /** The version of ZAP to download, if zapInstallDir is not defined. */
    String version = '2.7.0'

    /**
     * The directory where ZAP is installed. The directory must contains zap.sh or zap.bat. If not
     * defined, ZAP will be downloaded into the ~/.gradle directory.
     */
    String zapInstallDir = ''

    /**
     * The proxy port on which ZAP is listening. When empty, a random port will be chosen. If a port is
     * specified and there is a ZAP process listening on the port, the plugin will use the running instance.
     * In this case, #apiKey will also need to be set.
     */
    String proxyPort = ''

    /**
     * Comma and/or space separated list: json, html, xml, md or leave empty for all.
     */
    String reportFormat = ''

    /**
     * The name of report files, without extension. The reports will be put into the project buildDir.
     */
    String reportOutputPath = 'reports/zap/zapReport'

    /**
     * The base application URL for spidering.
     */
    String applicationUrl = ''

    /**
     * The timeout, in seconds, to wait for the active scan or spidering to complete. The timeout applies
     * to each spider and scan task, it's not cumulative. A value of 0 will not timeout.
     */
    String activeScanTimeout = '300'

    /**
     * The API key for authenticating to ZAP. Defaults to a random value and is automatically used in
     * tasks and when calling the #api() and #api(Closure) methods. This is required when using an
     * already running ZAP.
     */
    String apiKey = UUID.randomUUID()

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
    List<String> parameters = []

    /**
     * ZAP process started by the ZapStart task. May not be populated if the #proxyPort points to an already
     * running ZAP.
     */
    protected Process zapProc

    /**
     * Get an instance of ClienApi with the host, port and API key set.
     */
    ClientApi api() {
        new ClientApi('localhost', proxyPort as int, apiKey)
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
