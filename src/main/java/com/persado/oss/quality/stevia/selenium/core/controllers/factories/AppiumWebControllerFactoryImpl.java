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

import com.persado.oss.quality.stevia.selenium.core.SteviaContext;
import com.persado.oss.quality.stevia.selenium.core.WebController;
import com.persado.oss.quality.stevia.selenium.core.controllers.AppiumWebController;
import com.persado.oss.quality.stevia.selenium.core.controllers.SteviaWebControllerFactory;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class AppiumWebControllerFactoryImpl implements WebControllerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AppiumWebControllerFactoryImpl.class);
    public static final String APPIUM_SERVER_URL = "http://" + SteviaContext.getParam(SteviaWebControllerFactory.RC_HOST) + ":" + SteviaContext.getParam(SteviaWebControllerFactory.RC_PORT) + "/wd/hub";

    @Override
    public WebController initialize(ApplicationContext context, WebController controller) {
        AppiumWebController appiumController = (AppiumWebController) controller;
        AppiumDriver driver = null;

        DesiredCapabilities capabilities = new DesiredCapabilities();

        setupCommonCapabilities(capabilities);
        setCloudServiceCapabilities(capabilities);


        LOG.info("Appium Desired capabilities {}", new Object[]{capabilities});


        String platform = SteviaContext.getParam(MobileCapabilityType.PLATFORM_NAME);
        setCapabilitiesForPlatform(capabilities, platform);
        driver = getDriverForPlatform(capabilities, platform);
        driver.setFileDetector(new LocalFileDetector());

        if (variableExists(SteviaWebControllerFactory.TARGET_HOST_URL) && variableExists(SteviaWebControllerFactory.BROWSER)) {
            driver.get(SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL));
        }

        appiumController.setDriver(driver);
        return appiumController;
    }

    private void setCloudServiceCapabilities(DesiredCapabilities capabilities) {
        String cloudService = SteviaContext.getParam("cloudService");
        if (cloudService.equalsIgnoreCase("SauceLabs")) {
            //Sauce Labs parameters
            setupSauceLabsParams(capabilities);
        }

        if (cloudService.equalsIgnoreCase("Testdroid")) {
            //TestDroid parameters
            setupTestDroidParameters(capabilities);
        }

        if (cloudService.equalsIgnoreCase("SeleniumGrid")) {
            //Selenium Grid test level parameters
            setupSeleniumGridParameters(capabilities);
        }
    }

    private void setCapabilitiesForPlatform(DesiredCapabilities capabilities, String platform){
        if (platform.compareTo("Android") == 0) {
            setupAndroidCapabilities(capabilities);
        } else if (platform.compareTo("iOS") == 0) {
            setupIOSCapabilities(capabilities);
        }
    }

    private AppiumDriver getAppiumDriverForPlatform(String platform, DesiredCapabilities capabilities) throws MalformedURLException {
        if (platform.compareTo("Android") == 0) {
            return getAndroidDriverWithCapabilities(capabilities);
        } else if (platform.compareTo("iOS") == 0) {
            return getIOSDriverWithCapabilities(capabilities);
        }
        throw new IllegalArgumentException(String.format("Driver for platform %s not found", platform));
    }

    private AppiumDriver getDriverForPlatform(DesiredCapabilities capabilities, String platform) {
        AppiumDriver driver;
        try {
            driver = getAppiumDriverForPlatform(platform, capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return driver;
    }

    private IOSDriver getIOSDriverWithCapabilities(DesiredCapabilities capabilities) throws MalformedURLException {
        return new IOSDriver(buildAppiumUrl(), capabilities);
    }

    private URL buildAppiumUrl() throws MalformedURLException {
        String rcHost = SteviaContext.getParam(SteviaWebControllerFactory.RC_HOST);
        String rcPort = SteviaContext.getParam(SteviaWebControllerFactory.RC_PORT);
        String url = String.format("http://%s:%s/wd/hub", rcHost, rcPort);
        return new URL(url);
    }

    private AndroidDriver getAndroidDriverWithCapabilities(DesiredCapabilities capabilities) throws MalformedURLException {
        return new AndroidDriver(buildAppiumUrl(), capabilities);
    }

    private void setupSeleniumGridParameters(DesiredCapabilities capabilities) {
        setCapabilitiesInList(capabilities, WantedAppiumCapabilities.SELENIUM_GRID_CAPABILITIES);
    }

    private boolean variableExists(String param) {
        return !StringUtils.isEmpty(SteviaContext.getParam(param));
    }

    private void setupIOSCapabilities(DesiredCapabilities capabilities) {
        if (SteviaContext.getParam("runOnRealDevice").equals("true")) {
            setCapabilityIfExists(capabilities, "realDeviceLogger");
            setCapabilityIfExists(capabilities, IOSMobileCapabilityType.XCODE_CONFIG_FILE);
        }

        setCapabilitiesInList(capabilities, WantedAppiumCapabilities.IOS_DEFAULT_CAPABILITIES);
        if (variableExists(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER)) {
            capabilities.setCapability(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER, Boolean.parseBoolean(SteviaContext.getParam(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER)));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL, Integer.parseInt(SteviaContext.getParam(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL)));
        }
    }

    private void setCapabilitiesInList(DesiredCapabilities capabilities, List<String> capList) {
        for (String cap : capList) {
            setCapabilityIfExists(capabilities, cap);
        }
    }

    private void setCapabilityIfExists(DesiredCapabilities capabilities, String capabilityToSet) {
        if (variableExists(capabilityToSet)) {
            capabilities.setCapability(capabilityToSet, SteviaContext.getParam(capabilityToSet));
        }
    }

    private void setupCommonCapabilities(DesiredCapabilities capabilities) {
        if (variableExists(SteviaWebControllerFactory.BROWSER)) {
            capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, SteviaContext.getParam(SteviaWebControllerFactory.BROWSER));
        }
        if (variableExists(MobileCapabilityType.NEW_COMMAND_TIMEOUT)) {
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, Integer.parseInt(SteviaContext.getParam(MobileCapabilityType.NEW_COMMAND_TIMEOUT)));
        }

        setCapabilitiesInList(capabilities, WantedAppiumCapabilities.COMMON_CAPABILITIES);
    }

    private void setupAndroidCapabilities(DesiredCapabilities capabilities) {
        capabilities.setCapability(AndroidMobileCapabilityType.RECREATE_CHROME_DRIVER_SESSIONS, true);

        setCapabilitiesInList(capabilities, WantedAppiumCapabilities.ANDROID_DEFAULT_CAPABILITIES);
        if (variableExists("skipUnlock")) {
            capabilities.setCapability("skipUnlock", SteviaContext.getParam("skipUnlock"));
        }
        if (variableExists(AndroidMobileCapabilityType.NO_SIGN)) {
            capabilities.setCapability(AndroidMobileCapabilityType.NO_SIGN, Boolean.parseBoolean(SteviaContext.getParam(AndroidMobileCapabilityType.NO_SIGN)));
        }
    }

    private void setupTestDroidParameters(DesiredCapabilities capabilities) {
        setCapabilitiesInList(capabilities, WantedAppiumCapabilities.TEST_DROID_CAPABILITIES);
    }

    private void setupSauceLabsParams(DesiredCapabilities capabilities) {
        setCapabilitiesInList(capabilities, WantedAppiumCapabilities.SAUCE_LABS_CAPABILITIES);
    }

    @Override
    public String getBeanName() {
        return "appiumController";
    }
}
