package com.patdouble.gradle.zap

import groovy.transform.CompileDynamic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.zaproxy.clientapi.core.ApiResponse
import org.zaproxy.clientapi.core.ClientApiException

/**
 * Logs information about the ZAP server configuration.
 */
@CompileDynamic
class ZapInfo extends DefaultTask {

    @SuppressWarnings('LineLength')
    ZapInfo() {
        group = ZapPlugin.GROUP
        description = 'Logs information about the ZAP server configuration.'
    }

    CharSequence apiAsString(Closure closure) {
        try {
            ((ApiResponse) closure.call()).toString(0)
        } catch (ClientApiException e) {
            "${e}, ${e.code}, ${e.detail}"
        }
    }

    @TaskAction
    @SuppressWarnings('UnusedMethod')
    void info() {
        project.zapConfig {
            api {
                logger.quiet "Auth = ${apiAsString { authentication.getSupportedAuthenticationMethods() }}"
                authentication.getSupportedAuthenticationMethods().items.collect { it.value as String }.each { String authMethod ->
                    logger.quiet "Auth method ${authMethod} = ${apiAsString { authentication.getAuthenticationMethodConfigParams(authMethod) }}"
                }
                logger.quiet "Script engines = ${apiAsString { script.listEngines() }}"
                logger.quiet "Scripts = ${apiAsString { script.listScripts() }}"
                logger.quiet "Contexts = ${apiAsString { context.contextList() }}"
                context.contextList().items.collect { it.value as String }.each { String name ->
                    logger.quiet "Context ${name} = ${apiAsString { context.context(name) }}"
                    String contextId = context.context(name).valuesMap['id'].value
                    logger.quiet "Users for ${name} = ${apiAsString { users.usersList(contextId) }}"
                    logger.quiet "Auth for ${name} = ${apiAsString { authentication.getAuthenticationMethod(contextId) }}"
                    logger.quiet "Auth Config Params for ${name} = ${apiAsString { users.getAuthenticationCredentialsConfigParams(contextId) }}"
                }
            }
        }
    }

}
