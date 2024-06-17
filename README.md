stevia <img src="https://raw.github.com/persado/stevia/master/doc/stevia-logo.png" align="right" width="80">
======

## Stevia Quick Start (10 minutes) guide

Our 10-minutes guide for using Stevia is in our [wiki](https://github.com/persado/stevia/wiki/Stevia-10-minute-Quick-Start). Read on and start coding!

## Features

The following features are supported in the current Stevia build (new features have a version next to them):
* Works with latest Selenium libraries (4.20+) and Spring 3.2.x (4.0 coming soon)
* Works with stable TestNG tested for parallel running
* Supports both Webdriver and Selenium RC, standalone or Grid via easy configuration
* Supports TestNG with parallel test execution (each thread has its own browser/session)
* Versatile extension mechanism allows users of Stevia to extend it by:
    * [Controllers via Factory Pattern](https://github.com/persado/stevia/wiki/Extending-web-controller-support) (we load `META-INF/spring/stevia-extensions-drivers-*.xml` from classpath)
    * Navigation Beans, PageObjects, Spring beans (we load `META-INF/spring/test-beans-*.xml` from classpath)
    * Connectors for Rally, JIRA, Testlink (we load `META-INF/spring/stevia-extensions-connectors-*.xml` from classpath)
* Full logging support using ReportNG, with 
    * [screenshots of browser for tests that failed](http://seleniumtestingworld.blogspot.gr/2013/03/reportng-enrichment-with-screenshots.html)
    * actions reporting on test report log and HTML report
* Realtime(!) highlighting of locators, (accessed = yellow, success = green, failure = red)
* [Extended "By" mechanism to support SizzleCSS](http://seleniumtestingworld.blogspot.gr/2013/01/adding-sizzle-css-selector-library-and.html) on Webdriver
* Detailed "Verify" class with lots of assertions pre-coded
* Supports for SSH/SFTP via utility classes
* Supports for HTTP GET,POST with Jetty high-performance, multi-threaded helper and cookies support
* Supports thread-level common user configuration and state across Tests (within Stevia thread context)
* Supports Annotations (Java 5+)
    * RunsWithController - allows a different controller (different browser or session) to run a @Test method or class
    * [Preconditions](http://seleniumtestingworld.blogspot.gr/2014/04/concurrency-testing-made-easy.html) - allows methods to be called (optionally with different controller) before @Test method
    * Postconditions - similar to @Precondition but after the @Test method.
* Lots of other minor features

## Release Management - Maven release plugin

```console
mvn clean -Darguments=-DskipTests release:prepare
```

```console
mvn clean -Darguments=-DskipTests release:perform
```

If you changed your mind, or something went wrong:
```console
mvn release:rollback
```

```console
mvn release:clean
```

Upon release artifact will also be uploaded in [Gcloud Artifacts registry](https://console.cloud.google.com/artifacts/maven/staging-artifacts-786a/us-east4/maven-local/com.persado.oss.quality.stevia:stevia-core?hl=en&project=staging-artifacts-786a)

## Deploying a Snapshot to Gcloud Artifactory

```console
mvn clean deploy -DskipTests
```
