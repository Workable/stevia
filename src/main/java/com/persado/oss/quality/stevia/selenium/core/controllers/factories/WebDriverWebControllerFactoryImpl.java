package com.persado.oss.quality.stevia.selenium.core.controllers.factories;

/*
 * #%L
 * Stevia QA Framework - Core
 * %%
 * Copyright (C) 2013 - 2014 Persado
 * %%
 * Copyright (c) Persado Intellectual Property Limited. All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  
 * * Neither the name of the Persado Intellectual Property Limited nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.opera.core.systems.OperaDriver;
import com.persado.oss.quality.stevia.selenium.core.SteviaContext;
import com.persado.oss.quality.stevia.selenium.core.WebController;
import com.persado.oss.quality.stevia.selenium.core.controllers.SteviaWebControllerFactory;
import com.persado.oss.quality.stevia.selenium.core.controllers.WebDriverWebController;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.testng.Assert;

import java.io.File;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebDriverWebControllerFactoryImpl implements WebControllerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(WebDriverWebControllerFactoryImpl.class);

    @Override
    public WebController initialize(ApplicationContext context, WebController controller) {
        WebDriverWebController wdController = (WebDriverWebController) controller;
        WebDriver driver = null;
        if (SteviaContext.getParam(SteviaWebControllerFactory.DEBUGGING).compareTo(SteviaWebControllerFactory.TRUE) == 0) { // debug=on
            if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER) == null || SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("firefox") == 0
                    || SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).isEmpty()) {
                driver = setUpFirefoxDriver();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("chrome") == 0) {
                driver = setUpChromeDriver();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("iexplorer") == 0) {
                LOG.info("Debug enabled, using InternetExplorerDriver");
                driver = new InternetExplorerDriver();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("safari") == 0) {
                LOG.info("Debug enabled, using SafariDriver");
                driver = new SafariDriver();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("opera") == 0) {
                LOG.info("Debug enabled, using OperaDriver");
                driver = new OperaDriver();
            } else {
                throw new IllegalArgumentException(SteviaWebControllerFactory.WRONG_BROWSER_PARAMETER);
            }

        } else { // debug=off
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            if (!StringUtils.isEmpty(SteviaContext.getParam("screenResolution"))) {
                LOG.info("Set screen resolution to " + SteviaContext.getParam("screenResolution"));
                desiredCapabilities.setCapability("screenResolution", SteviaContext.getParam("screenResolution"));
            }
            if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER) == null || SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("firefox") == 0
                    || SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).isEmpty()) {
                LOG.info("Debug OFF, using a RemoteWebDriver with Firefox capabilities");
                desiredCapabilities = DesiredCapabilities.firefox();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("chrome") == 0) {
                LOG.info("Debug OFF, using a RemoteWebDriver with Chrome capabilities");
                // possible fix for https://code.google.com/p/chromedriver/issues/detail?id=799
                desiredCapabilities = DesiredCapabilities.chrome();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("test-type");
                //set window size
                if (SteviaContext.getParam("headlessChrome") != null && SteviaContext.getParam("headlessChrome").equals("true")) {
                    options.addArguments("--headless");
                    options.addArguments("--disable-gpu");
                }
                if (SteviaContext.getParam("windowSize") != null) {
                    options.addArguments("--window-size=" + SteviaContext.getParam("windowSize").replace("x", ","));
                    LOG.info("Setting window size to "+SteviaContext.getParam("windowSize"));
                } else {
                    options.addArguments("--window-size=1920,1080");
                    LOG.info("Setting window size to 1920x1080");
                }

                if (SteviaContext.getParam("chromeExtensions") != null) {
                    List<String> extensionPaths = Arrays.asList(SteviaContext.getParam("chromeExtensions").split(","));
                    for (String path : extensionPaths) {
                        LOG.info("Use chrome with extension: " + path);
                        options.addExtensions(new File(path));
                    }
                }

                if (SteviaContext.getParam("chromeDriverProxy") != null && SteviaContext.getParam("chromeDriverProxy").equals("true")){
                    try {
                        String hostIp = Inet4Address.getLocalHost().getHostAddress();
                        options.addArguments("--proxy-server="+hostIp+":" + PortProber.findFreePort());
                        LOG.info("Proxy functionality enabled for chromedriver and running on: " + hostIp+":5439");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        Assert.fail("invalid Host Address");
                    }
                    //            capabilities.setCapability(CapabilityType.PROXY, );
                }

                desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("iexplorer") == 0) {
                LOG.info("Debug OFF, using a RemoteWebDriver with Internet Explorer capabilities");
                desiredCapabilities = DesiredCapabilities.internetExplorer();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("safari") == 0) {
                LOG.info("Debug OFF, using a RemoteWebDriver with Safari capabilities");
                desiredCapabilities = DesiredCapabilities.safari();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("opera") == 0) {
                LOG.info("Debug OFF, using a RemoteWebDriver with Opera capabilities");
                desiredCapabilities = DesiredCapabilities.opera();
            } else if (SteviaContext.getParam(SteviaWebControllerFactory.BROWSER).compareTo("phantomjs") == 0) {
                LOG.info("Debug OFF, using phantomjs driver");
                desiredCapabilities = DesiredCapabilities.phantomjs();
            } else {
                throw new IllegalArgumentException(SteviaWebControllerFactory.WRONG_BROWSER_PARAMETER);
            }
            if (!StringUtils.isEmpty(SteviaContext.getParam("browserVersion"))) {
                LOG.info("Version: " + SteviaContext.getParam("browserVersion"));
                desiredCapabilities.setVersion(SteviaContext.getParam("browserVersion"));
            }
            if (!StringUtils.isEmpty(SteviaContext.getParam("platform"))) {
                LOG.info("Operating System: " + SteviaContext.getParam("platform"));
                desiredCapabilities.setPlatform(Platform.valueOf(SteviaContext.getParam("platform")));
            }
            if (!StringUtils.isEmpty(SteviaContext.getParam("recordVideo"))) {
                String record_video = SteviaContext.getParam("recordVideo");
                LOG.info("Set recording video to: " + SteviaContext.getParam("recordVideo"));
                if (record_video.equalsIgnoreCase("True")) {
                    desiredCapabilities.setCapability("video", "True");
                } else {
                    desiredCapabilities.setCapability("video", "False");
                }
            }

            final DesiredCapabilities wdCapabilities = desiredCapabilities;
            final String wdHost = SteviaContext.getParam(SteviaWebControllerFactory.RC_HOST) + ":" + SteviaContext.getParam(SteviaWebControllerFactory.RC_PORT);
            CompletableFuture<WebDriver> wd = CompletableFuture.supplyAsync(() -> getRemoteWebDriver(wdHost, wdCapabilities));
            try {
                driver = wd.get(Integer.valueOf(SteviaContext.getParam("nodeTimeout")), TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getMessage());
            } catch (TimeoutException e) {
                throw new RuntimeException("Timeout of " + Integer.valueOf(SteviaContext.getParam("nodeTimeout")) + " minutes reached waiting for a hub node to receive the request");
            }
            ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        }

        if (SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL) != null) {
            driver.get(SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL));
        }
        // driver.manage().window().maximize();
        wdController.setDriver(driver);
        if (SteviaContext.getParam(SteviaWebControllerFactory.ACTIONS_LOGGING).compareTo(SteviaWebControllerFactory.TRUE) == 0) {
            wdController.enableActionsLogging();
        }
        return wdController;
    }

    @Override
    public String getBeanName() {
        return "webDriverController";
    }

    private WebDriver getRemoteWebDriver(String rcHost, DesiredCapabilities desiredCapabilities) {
        WebDriver driver;
        Augmenter augmenter = new Augmenter(); // adds screenshot capability to a default webdriver.
        try {
            driver = augmenter.augment(new RemoteWebDriver(new URL("http://" + rcHost + "/wd/hub"), desiredCapabilities));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return driver;
    }

    private WebDriver setUpFirefoxDriver() {
        WebDriver driver;
        String profileToUse = SteviaContext.getParam(SteviaWebControllerFactory.PROFILE);
        if (profileToUse == null || profileToUse.isEmpty()) {
            LOG.info("Debug enabled, using Firefox Driver");
            driver = new FirefoxDriver();
        } else {
            LOG.info("Debug enabled, using a local Firefox profile {} with FirefoxDriver", profileToUse);
            ProfilesIni allProfiles = new ProfilesIni();
            FirefoxProfile ffProfile = allProfiles.getProfile(profileToUse);
            driver = new FirefoxDriver(ffProfile);
        }
        return driver;
    }

    private WebDriver setUpChromeDriver() {
        WebDriver driver = null;
        LOG.info("Debug enabled, using ChromeDriver");
        // possible fix for https://code.google.com/p/chromedriver/issues/detail?id=799
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        //Added support for headless chrome mode
        if (SteviaContext.getParam("headlessChrome") != null && SteviaContext.getParam("headlessChrome").equals("true")) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
        }

        if (SteviaContext.getParam("windowSize") != null) {
            options.addArguments("--window-size=" + SteviaContext.getParam("windowSize").replace("x", ","));
        } else {
            options.addArguments("--window-size=1920,1080");
        }

        if (SteviaContext.getParam("chromeExtensions") != null) {
            List<String> extensionPaths = Arrays.asList(SteviaContext.getParam("chromeExtensions").split(","));
            for (String path : extensionPaths) {
                LOG.info("Use chrome with extension: " + path);
                options.addExtensions(new File(path));
            }
        }

        if (SteviaContext.getParam("chromeDriverProxy") != null && SteviaContext.getParam("chromeDriverProxy").equals("true")){
            try {
                String hostIp = Inet4Address.getLocalHost().getHostAddress();
                options.addArguments("--proxy-server="+hostIp+":5439");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Assert.fail("invalid Host Address");
            }
            //            capabilities.setCapability(CapabilityType.PROXY, );
        }

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        CompletableFuture<WebDriver> wd = CompletableFuture.supplyAsync(() -> getChromeDriver(capabilities));
        try {
            driver = wd.get(Integer.valueOf(SteviaContext.getParam("nodeTimeout")), TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout of " + Integer.valueOf(SteviaContext.getParam("nodeTimeout")) + " minutes reached waiting for a local chrome to come up");
        }
        return driver;
    }

    private WebDriver getChromeDriver(DesiredCapabilities desiredCapabilities) {
        return new ChromeDriver(desiredCapabilities);
    }
}
