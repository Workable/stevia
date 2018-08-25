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
            if (variableExists("realDeviceLogger")) {
                capabilities.setCapability("realDeviceLogger", SteviaContext.getParam("realDeviceLogger"));
            }
            if (variableExists(IOSMobileCapabilityType.XCODE_CONFIG_FILE)) {
                capabilities.setCapability(IOSMobileCapabilityType.XCODE_CONFIG_FILE, SteviaContext.getParam(IOSMobileCapabilityType.XCODE_CONFIG_FILE));
            }
        }
        if (variableExists(IOSMobileCapabilityType.USE_PREBUILT_WDA)) {
            capabilities.setCapability(IOSMobileCapabilityType.USE_PREBUILT_WDA, SteviaContext.getParam(IOSMobileCapabilityType.USE_PREBUILT_WDA));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_LOCAL_PORT)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_LOCAL_PORT, Integer.parseInt(SteviaContext.getParam(IOSMobileCapabilityType.WDA_LOCAL_PORT)));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_CONNECTION_TIMEOUT)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_CONNECTION_TIMEOUT, SteviaContext.getParam(IOSMobileCapabilityType.WDA_CONNECTION_TIMEOUT));
        }
        if (variableExists(IOSMobileCapabilityType.USE_NEW_WDA)) {
            capabilities.setCapability(IOSMobileCapabilityType.USE_NEW_WDA, SteviaContext.getParam(IOSMobileCapabilityType.USE_NEW_WDA));
        }
        if (variableExists(IOSMobileCapabilityType.XCODE_ORG_ID)) {
            capabilities.setCapability(IOSMobileCapabilityType.XCODE_ORG_ID, SteviaContext.getParam(IOSMobileCapabilityType.XCODE_ORG_ID));
        }
        if (variableExists(IOSMobileCapabilityType.XCODE_SIGNING_ID)) {
            capabilities.setCapability(IOSMobileCapabilityType.XCODE_SIGNING_ID, SteviaContext.getParam(IOSMobileCapabilityType.XCODE_SIGNING_ID));
        }
        if (variableExists(IOSMobileCapabilityType.SHOW_IOS_LOG)) {
            capabilities.setCapability(IOSMobileCapabilityType.SHOW_IOS_LOG, SteviaContext.getParam(IOSMobileCapabilityType.SHOW_IOS_LOG));
        }
        if (variableExists(IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS)) {
            capabilities.setCapability(IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS, SteviaContext.getParam(IOSMobileCapabilityType.AUTO_ACCEPT_ALERTS));
        }
        if (variableExists(IOSMobileCapabilityType.AUTO_DISMISS_ALERTS)) {
            capabilities.setCapability(IOSMobileCapabilityType.AUTO_DISMISS_ALERTS, SteviaContext.getParam(IOSMobileCapabilityType.AUTO_DISMISS_ALERTS));
        }
        if (variableExists("useJSONSource")) {
            capabilities.setCapability("useJSONSource", SteviaContext.getParam("useJSONSource"));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_LAUNCH_TIMEOUT)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_LAUNCH_TIMEOUT, SteviaContext.getParam(IOSMobileCapabilityType.WDA_LAUNCH_TIMEOUT));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_STARTUP_RETRIES)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_STARTUP_RETRIES, SteviaContext.getParam(IOSMobileCapabilityType.WDA_STARTUP_RETRIES));
        }
        if (variableExists(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER)) {
            capabilities.setCapability(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER, Boolean.parseBoolean(SteviaContext.getParam(IOSMobileCapabilityType.SHOULD_USE_SINGLETON_TESTMANAGER)));
        }
        if (variableExists(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL)) {
            capabilities.setCapability(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL, Integer.parseInt(SteviaContext.getParam(IOSMobileCapabilityType.WDA_STARTUP_RETRY_INTERVAL)));
        }
    }


    private void setupCommonCapabilities(DesiredCapabilities capabilities) {
        if (variableExists(MobileCapabilityType.DEVICE_NAME)) {
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, SteviaContext.getParam(MobileCapabilityType.DEVICE_NAME));
        }
        if (variableExists(MobileCapabilityType.UDID)) {
            capabilities.setCapability(MobileCapabilityType.UDID, SteviaContext.getParam(MobileCapabilityType.UDID));
        }
        if (variableExists(MobileCapabilityType.PLATFORM_NAME)) {
            capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, SteviaContext.getParam(MobileCapabilityType.PLATFORM_NAME));
        }
        if (variableExists(MobileCapabilityType.PLATFORM_VERSION)) {
            capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, SteviaContext.getParam(MobileCapabilityType.PLATFORM_VERSION));
        }
        if (variableExists(SteviaWebControllerFactory.BROWSER)) {
            capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, SteviaContext.getParam(SteviaWebControllerFactory.BROWSER));
        }
        if (variableExists(MobileCapabilityType.APP)) {
            capabilities.setCapability(MobileCapabilityType.APP, SteviaContext.getParam(MobileCapabilityType.APP));
        }
        if (variableExists(MobileCapabilityType.AUTO_WEBVIEW)) {
            capabilities.setCapability(MobileCapabilityType.AUTO_WEBVIEW, SteviaContext.getParam(MobileCapabilityType.AUTO_WEBVIEW));
        }
        if (variableExists(MobileCapabilityType.NEW_COMMAND_TIMEOUT)) {
            capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, Integer.parseInt(SteviaContext.getParam(MobileCapabilityType.NEW_COMMAND_TIMEOUT)));
        }
        if (variableExists(MobileCapabilityType.AUTOMATION_NAME)) {
            capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, SteviaContext.getParam(MobileCapabilityType.AUTOMATION_NAME));
        }
        if (variableExists(MobileCapabilityType.NO_RESET)) {
            capabilities.setCapability(MobileCapabilityType.NO_RESET, SteviaContext.getParam(MobileCapabilityType.NO_RESET));
        }
        if (variableExists(MobileCapabilityType.CLEAR_SYSTEM_FILES)) {
            capabilities.setCapability(MobileCapabilityType.CLEAR_SYSTEM_FILES, SteviaContext.getParam(MobileCapabilityType.CLEAR_SYSTEM_FILES));
        }
    }

    private void setupAndroidCapabilities(DesiredCapabilities capabilities) {
        capabilities.setCapability(AndroidMobileCapabilityType.RECREATE_CHROME_DRIVER_SESSIONS, true);
        if (variableExists(AndroidMobileCapabilityType.APP_PACKAGE)) {
            capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, SteviaContext.getParam(AndroidMobileCapabilityType.APP_PACKAGE));
        }
        if (variableExists(AndroidMobileCapabilityType.APP_WAIT_PACKAGE)) {
            capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_PACKAGE, SteviaContext.getParam(AndroidMobileCapabilityType.APP_WAIT_PACKAGE));
        }
        if (variableExists(AndroidMobileCapabilityType.APP_ACTIVITY)) {
            capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, SteviaContext.getParam(AndroidMobileCapabilityType.APP_ACTIVITY));
        }
        if (variableExists(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY)) {
            capabilities.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, SteviaContext.getParam(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY));
        }
        if (variableExists(AndroidMobileCapabilityType.ADB_PORT)) {
            capabilities.setCapability(AndroidMobileCapabilityType.ADB_PORT, SteviaContext.getParam(AndroidMobileCapabilityType.ADB_PORT));
        }
        if (variableExists(AndroidMobileCapabilityType.SYSTEM_PORT)) {
            capabilities.setCapability(AndroidMobileCapabilityType.SYSTEM_PORT, SteviaContext.getParam(AndroidMobileCapabilityType.SYSTEM_PORT));
        }
        if (variableExists(AndroidMobileCapabilityType.NO_SIGN)) {
            capabilities.setCapability(AndroidMobileCapabilityType.NO_SIGN, Boolean.parseBoolean(SteviaContext.getParam(AndroidMobileCapabilityType.NO_SIGN)));
        }
        if (variableExists("skipUnlock")) {
            capabilities.setCapability("skipUnlock", Boolean.parseBoolean(SteviaContext.getParam("skipUnlock")));
        }
        if (variableExists(AndroidMobileCapabilityType.USE_KEYSTORE)) {
            capabilities.setCapability(AndroidMobileCapabilityType.USE_KEYSTORE, SteviaContext.getParam(AndroidMobileCapabilityType.USE_KEYSTORE));
        }
        if (variableExists(AndroidMobileCapabilityType.KEYSTORE_PATH)) {
            capabilities.setCapability(AndroidMobileCapabilityType.KEYSTORE_PATH, SteviaContext.getParam(AndroidMobileCapabilityType.KEYSTORE_PATH));
        }
        if (variableExists(AndroidMobileCapabilityType.KEYSTORE_PASSWORD)) {
            capabilities.setCapability(AndroidMobileCapabilityType.KEYSTORE_PASSWORD, SteviaContext.getParam(AndroidMobileCapabilityType.KEYSTORE_PASSWORD));
        }
        if (variableExists(AndroidMobileCapabilityType.KEY_ALIAS)) {
            capabilities.setCapability(AndroidMobileCapabilityType.KEY_ALIAS, SteviaContext.getParam(AndroidMobileCapabilityType.KEY_ALIAS));
        }
        if (variableExists(AndroidMobileCapabilityType.KEY_PASSWORD)) {
            capabilities.setCapability(AndroidMobileCapabilityType.KEY_PASSWORD, SteviaContext.getParam(AndroidMobileCapabilityType.KEY_PASSWORD));
        }
        if (variableExists(AndroidMobileCapabilityType.ANDROID_COVERAGE)) {
            capabilities.setCapability(AndroidMobileCapabilityType.ANDROID_COVERAGE, SteviaContext.getParam(AndroidMobileCapabilityType.ANDROID_COVERAGE));
        }

    }

    private void setupTestDroidParameters(DesiredCapabilities capabilities) {
        if (SteviaContext.getParam("cloudService").equalsIgnoreCase("Testdroid")) {
            if (variableExists("testdroid_username")) {
                capabilities.setCapability("testdroid_username", SteviaContext.getParam("testdroid_username"));
            }
            if (variableExists("testdroid_password")) {
                capabilities.setCapability("testdroid_password", SteviaContext.getParam("testdroid_password"));
            }
            if (variableExists("testdroid_apiKey")) {
                capabilities.setCapability("testdroid_apiKey", SteviaContext.getParam("testdroid_apiKey"));
            }
            if (variableExists("testdroid_target")) {
                capabilities.setCapability("testdroid_target", SteviaContext.getParam("testdroid_target"));
            }
            if (variableExists("testdroid_project")) {
                capabilities.setCapability("testdroid_project", SteviaContext.getParam("testdroid_project"));
            }
            if (variableExists("testdroid_testrun")) {
                capabilities.setCapability("testdroid_testrun", SteviaContext.getParam("testdroid_testrun"));
            }
            if (variableExists("testdroid_device")) {
                capabilities.setCapability("testdroid_device", SteviaContext.getParam("testdroid_device"));
            }
            if (variableExists("testdroid_app")) {
                capabilities.setCapability("testdroid_app", SteviaContext.getParam("testdroid_app"));
            }
        }
    }

    private void setAppSauceLabsParams(DesiredCapabilities capabilities) {
        if (SteviaContext.getParam("cloudService").equalsIgnoreCase("SauceLabs")) {
            if (variableExists("username")) {
                capabilities.setCapability("username", SteviaContext.getParam("username"));
            }
            if (variableExists("access-key")) {
                capabilities.setCapability("access-key", SteviaContext.getParam("access-key"));
            }
            if (variableExists("deviceType")) {
                capabilities.setCapability("deviceType", SteviaContext.getParam("deviceType"));
            }
            if (variableExists("appiumVersion")) {
                capabilities.setCapability("appiumVersion", SteviaContext.getParam("appiumVersion"));
            }
        }
    }

    @Override
    public String getBeanName() {
        return "appiumController";
    }


}
