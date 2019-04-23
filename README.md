# ZAP Gradle Plugin

Automate web application dynamic security scans using [OWASP ZAP](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project).

Nearly all operations of ZAP can be automated using this plugin thanks to the ZAP API. Spidering and Active Scan are provided as tasks.
 
Originally based off plugin found here: https://github.com/PROSPricing/zap-gradle-plugin

## TL;DR

```groovy
plugins { 
    id 'com.patdouble.gradle.zap' version '2.1.0'
}

zapConfig {
    applicationUrl = "http://attackme.example.com:8080"
}
```

```bash
$ ./gradlew zapSpider zapActiveScan zapReport
$ ls -1 build/reports/zap
zapReport.err.log
zapReport.html
zapReport.json
zapReport.md
zapReport.out.log
zapReport.xml
```

## Examples

The `examples` directory contains a multi-project Gradle build using several popular vulnerable web applications. The `check` task in each project starts the application, runs the spider, active scan and produces reports. The reports are in the `build/reports/zap` directory inside each sub-project.

You need a working [Docker](https://docker.com) install to run the examples.

```bash
$ cd examples
$ ./gradlew check
$ ls */build/reports/zap
```

## Getting the Plugin

The plugin is available from the Gradle plugins repository using the usual methods.

```groovy
plugins {
    id 'com.patdouble.zap' version '2.1.0'
}
```

or

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.patdouble:zap-gradle-plugin:2.+"
  }
}

apply plugin: "com.patdouble.zap"
```

## Finding the ZAP Application

By default the plugin will download version 2.7.0 of OWASP ZAP and use it. You can specify a version by including the following in your `build.gradle`:

```groovy
zapConfig {
    version = "2.6.0"
}
```

If you'd rather use an already installed version, configure the install directory. The directory must include either
`zap.sh` or `zap.bat`, the script used to start ZAP.

```groovy
zapConfig {
    zapInstallDir =  "/path/to/ZAP/install/directory"
}
```

Weekly builds of ZAP are also supported. See [GitHub](https://github.com/zaproxy/zaproxy/releases) for available versions.

```groovy
zapConfig {
    version = 'w2019-04-15'
}
```


## Configure Your Application

At a minimum you must configure the URL of your application that ZAP is to scan.

```groovy
zapConfig {
    applicationUrl = "http://attackme.example.com:8080"
}
```

## Optional Properties
There are a few optional properties that may be specified within the zapConfig section of the gradle file to further tune your use of ZAP.

```groovy
zapConfig {
    // The port on which ZAP should run. Defaults to a free port.
    proxyPort = "9999"
    // The format(s) of the output report. Acceptable formats are JSON, HTML, MD and XML. Defaults to all.
    reportFormat = "JSON HTML"
    // The path of the report file to write from the zapReport task, without an extension. This path must be writable, subdirs will be created.
    reportOutputPath = "reports/dynamicscan"
    // The timeout for the active scanner process, in seconds. Defaults to 300 seconds.
    activeScanTimeout = "600" // 10 minutes
    // The API key to use when authenticating to the ZAP API. Defaults to a random value.
    apiKey = "mysecretapikey"
    // Additional command line parameters to use when starting ZAP. Run `zap.sh -help` to find available parameters.
    parameters = [ '-addoninstallall', '-addonupdate' ]
}
```

## Using an Already Running ZAP
It can be useful when debugging your build to use a ZAP instance running in GUI mode. This allows you to see what the ZAP plugin is causing ZAP to do and to experiment with options in the GUI to see how that affects the spidering, active scan, etc.

Start ZAP and fill in the `zapConfig` options below.
 
```groovy
zapConfig {
    // Preferences -> Options -> Local Proxies -> Port
    proxyPort = "9999"
    // Preferences -> Options -> API -> API Key
    apiKey = "mysecretapikey"
}
```

The `zapStart` task will check if ZAP responds on the configured port and if so will use the running process. In this case `zapStop` will not attempt to stop the process.

## Running ZAP with Tests
Instead of (or additional to) spidering, your existing functional tests can be used to populate the list of URLs the active scan attacks. Either run your test task between `zapStart` and `zapActiveScan` or configure Gradle task dependencies similarly. Assuming your functional test task is named `functionalTest`:

`./gradlew zapStart functionalTest zapActiveScan zapReport zapStop`

The `functionalTest` task (or whatever your project uses) will need to configure the proxy. See the next section for help. 

## Updating Tests to Use ZAP

In order for ZAP to see traffic to your app, it must be used as a proxy for those requests. Different testing tools will have different mechanisms for setting up a proxy for a given HTTP request. You can use the `zapConfig.proxyPort` value to pass the proxy port to your test. By default the port is randomly chosen. The `zapStart` task will populate the value.

```groovy
// build.gradle
test.systemProperty 'zap.port', zapConfig.proxyPort.get()
test.environment['ZAP_PORT'] = zapConfig.proxyPort.get()
```

Python with httplib2:
```python
http_con = httplib2.Http(proxy_info = httplib2.ProxyInfo(httplib2.socks.PROXY_TYPE_HTTP, 'localhost', os.environ['ZAP_PORT']))
```

Java/Groovy with URLConnection:
```java
URL url = new URL("http://attackme.example.com");
Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", Integer.parseInt(System.getProperty("zap.port")));
URLConnection connection = url.openConnection(proxy);
```

Ruby:
```ruby
proxy_addr = 'localhost'
proxy_port = ENV['ZAP_PORT'].to_i

Net::HTTP.new('attackme.example.com', nil, proxy_addr, proxy_port).start { |http|
  # ...
}
```

## Accessing ZAP API

Nearly everything in ZAP can be accessed via API. The plugin exposes the API using the `org.zaproxy:zap-clientapi` library. The `zapConfig.api` DSL gives you access to [`org.zaproxy.clientapi.core.ClientApi`](https://javadoc.io/doc/org.zaproxy/zap-clientapi/1.6.0).

```groovy
task zapSetup {
    doLast {
        zapConfig.api {
            spider.setOptionAcceptCookies(true)
            ascan.setOptionHandleAntiCSRFTokens(true)
        }
    }
}
```

Using the API is essential when the application requires authentication. See the `examples` directory.
 
## LICENSE
Copyright (c) 2018, Patrick Double. All right reserved.

Released under BSD-3 style license.

See http://opensource.org/licenses/BSD-3-Clause and LICENSE file for details.
