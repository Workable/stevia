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

public class AppiumWebControllerFactoryImpl implements WebControllerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AppiumWebControllerFactoryImpl.class);
    private boolean seleniumGridEnabled;

    @Override
    public WebController initialize(ApplicationContext context, WebController controller) {
        AppiumWebController appiumController = (AppiumWebController) controller;
        AppiumDriver driver = null;

        DesiredCapabilities capabilities = new DesiredCapabilities();

        setupCommonCapabilities(capabilities);

        //Sauce Labs parameters
        setAppSauceLabsParams(capabilities);

        //TestDroid parameters
        setupTestDroidParameters(capabilities);

        //Selenium Grid test level parameters
        setupSeleniumGridParameters(capabilities);

        LOG.info("Appium Desired capabilities {}", new Object[]{capabilities});


        if (SteviaContext.getParam(MobileCapabilityType.PLATFORM_NAME).compareTo("Android") == 0) {
            setupAndroidCapabilities(capabilities);
            try {
                driver = new AndroidDriver(new URL("http://" + SteviaContext.getParam(SteviaWebControllerFactory.RC_HOST) + ":" + SteviaContext.getParam(SteviaWebControllerFactory.RC_PORT) + "/wd/hub"), capabilities);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        if (SteviaContext.getParam(MobileCapabilityType.PLATFORM_NAME).compareTo("iOS") == 0) {
            try {
                setupIOSCapabilities(capabilities);
                driver = new IOSDriver(new URL("http://" + SteviaContext.getParam(SteviaWebControllerFactory.RC_HOST) + ":" + SteviaContext.getParam(SteviaWebControllerFactory.RC_PORT) + "/wd/hub"), capabilities);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        driver.setFileDetector(new LocalFileDetector());

        if (variableExists(SteviaWebControllerFactory.TARGET_HOST_URL) && variableExists(SteviaWebControllerFactory.BROWSER)) {
            driver.get(SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL));
        }

        appiumController.setDriver(driver);
        return appiumController;
    }

    private void setupSeleniumGridParameters(DesiredCapabilities capabilities) {

    }

    private boolean variableExists(String param) {
        return !StringUtils.isEmpty(SteviaContext.getParam(param));
    }

    private void setupIOSCapabilities(DesiredCapabilities capabilities) {
        if (SteviaContext.getParam("runOnRealDevice").equals("true")) {
            setCapabilityIfExists(capabilities, "realDeviceLogger");
            setCapabilityIfExists(capabilities, IOSMobileCapabilityType.XCODE_CONFIG_FILE);
        }
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.USE_PREBUILT_WDA);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.WDA_LOCAL_PORT);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.WDA_CONNECTION_TIMEOUT);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.USE_NEW_WDA);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.XCODE_ORG_ID);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.XCODE_SIGNING_ID);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.SHOW_IOS_LOG);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.AUTO_DISMISS_ALERTS);
        setCapabilityIfExists(capabilities, "useJSONSource");
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.WDA_LAUNCH_TIMEOUT);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.WDA_STARTUP_RETRIES);
        setCapabilityIfExists(capabilities, IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER);
        if (variableExists(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER)) {
            capabilities.setCapability(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER, Boolean.parseBoolean(SteviaContext.getParam(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER)));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL, Integer.parseInt(SteviaContext.getParam(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL)));
        }

    }

    private void setCapabilityIfExists(DesiredCapabilities capabilities, String capabilityToSet) {
        if (variableExists(capabilityToSet)) {
            capabilities.setCapability(capabilityToSet, SteviaContext.getParam(capabilityToSet));
        }
    }


    private void setupCommonCapabilities(DesiredCapabilities capabilities) {
        setCapabilityIfExists(capabilities, MobileCapabilityType.DEVICE_NAME);
        setCapabilityIfExists(capabilities, MobileCapabilityType.UDID);
        setCapabilityIfExists(capabilities, MobileCapabilityType.PLATFORM_NAME);
        setCapabilityIfExists(capabilities, MobileCapabilityType.PLATFORM_VERSION);
        if (variableExists(SteviaWebControllerFactory.BROWSER)) {
            capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, SteviaContext.getParam(SteviaWebControllerFactory.BROWSER));
        }
        setCapabilityIfExists(capabilities, MobileCapabilityType.APP);
        setCapabilityIfExists(capabilities, MobileCapabilityType.AUTO_WEBVIEW);
        if (variableExists(MobileCapabilityType.NEW_COMMAND_TIMEOUT)) {
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, Integer.parseInt(SteviaContext.getParam(MobileCapabilityType.NEW_COMMAND_TIMEOUT)));
        }
        setCapabilityIfExists(capabilities, MobileCapabilityType.AUTOMATION_NAME);
        setCapabilityIfExists(capabilities, MobileCapabilityType.NO_RESET);
        setCapabilityIfExists(capabilities, MobileCapabilityType.CLEAR_SYSTEM_FILES);
    }

    private void setupAndroidCapabilities(DesiredCapabilities capabilities) {
        capabilities.setCapability(AndroidMobileCapabilityType.RECREATE_CHROME_DRIVER_SESSIONS, true);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.APP_PACKAGE);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.APP_WAIT_PACKAGE);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.APP_ACTIVITY);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.APP_WAIT_ACTIVITY);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.ADB_PORT);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.SYSTEM_PORT);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.USE_KEYSTORE);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.KEYSTORE_PATH);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.KEYSTORE_PASSWORD);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.KEY_ALIAS);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.KEY_PASSWORD);
        setCapabilityIfExists(capabilities, AndroidMobileCapabilityType.ANDROID_COVERAGE);
        if (variableExists("skipUnlock")) {
            capabilities.setCapability("skipUnlock", SteviaContext.getParam("skipUnlock"));
        }
        if (variableExists(AndroidMobileCapabilityType.NO_SIGN)) {
            capabilities.setCapability(AndroidMobileCapabilityType.NO_SIGN, Boolean.parseBoolean(SteviaContext.getParam(AndroidMobileCapabilityType.NO_SIGN)));
        }

    }

    private void setupTestDroidParameters(DesiredCapabilities capabilities) {
        if (SteviaContext.getParam("cloudService").equalsIgnoreCase("Testdroid")) {
            setCapabilityIfExists(capabilities, "testdroid_username");
            setCapabilityIfExists(capabilities, "testdroid_password");
            setCapabilityIfExists(capabilities, "testdroid_apiKey");
            setCapabilityIfExists(capabilities, "testdroid_target");
            setCapabilityIfExists(capabilities, "testdroid_project");
            setCapabilityIfExists(capabilities, "testdroid_testrun");
            setCapabilityIfExists(capabilities, "testdroid_device");
            setCapabilityIfExists(capabilities, "testdroid_app");
        }
    }

    private void setAppSauceLabsParams(DesiredCapabilities capabilities) {
        if (SteviaContext.getParam("cloudService").equalsIgnoreCase("SauceLabs")) {
            setCapabilityIfExists(capabilities, "username");
            setCapabilityIfExists(capabilities, "access-key");
            setCapabilityIfExists(capabilities, "deviceType");
            setCapabilityIfExists(capabilities, "appiumVersion");
        }
    }

    @Override
    public String getBeanName() {
        return "appiumController";
    }


}
