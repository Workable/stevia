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
import com.persado.oss.quality.stevia.selenium.core.controllers.SteviaWebControllerFactory;
import com.persado.oss.quality.stevia.selenium.core.controllers.WebDriverWebController;
import com.persado.oss.quality.stevia.selenium.loggers.SteviaLogger;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.ClientConfig;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class WebDriverWebControllerFactoryImpl implements WebControllerFactory {

    @Override
    public WebController initialize(ApplicationContext context, WebController controller) throws InterruptedException, ExecutionException, TimeoutException, MalformedURLException, NoSuchFieldException, IllegalAccessException {
        WebDriverWebController wdController = (WebDriverWebController) controller;
        WebDriver driver = null;
        /**
         * Capabilities are now fetched from Stevia Context.
         * Capabilities are stored in Stevia Context inside qa-tests project
         * using the overriden testInitialisation in WorkableTestBase
         */

        Capabilities capabilities = SteviaContext.getCapabilities();

        // Handle type of webdriver based on "remote" param
        System.out.println("Initializing web driver with capabilities:" + SteviaContext.getCapabilities());
        String grid = SteviaContext.getParam("grid");
        final String wdHost = SteviaContext.getParam("rcUrl");
        GridInfo gridInfo;
        switch (grid) {
            case "gridlastic":
                gridInfo = setGridlasticOptions(capabilities, wdHost);
                driver = getRemoteWebDriver(gridInfo.gridUrl, capabilities, gridInfo);
                ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
                break;
            case "custom":
                gridInfo = GridInfo.parseUrl(wdHost, grid);
                driver = getRemoteWebDriver(gridInfo.gridUrl, capabilities, gridInfo);
                ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
                break;
            default:
                driver = getLocalDriver(capabilities);
                break;

        }
        //Navigate to the desired target host url
        if (SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL) != null) {
            driver.get(SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL));
        }
        wdController.setDriver(new Augmenter().augment(driver));
        return wdController;
    }

    @Override
    public String getBeanName() {
        return "webDriverController";
    }

    private WebDriver getRemoteWebDriver(String rcUrl, Capabilities desiredCapabilities, GridInfo gridInfo) throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        WebDriver driver = null;
        /**
         * When setting readTimeout RemoteWebDriver creation
         * we need to take into consideration the time needed for the Grid node to be spawned
         * Gridlastic suggests setting it to 10 minutes
         */

        /**
         * If variable exist, get value otherwise default value 10 minutes
         */
        String readTimeout = StringUtils.isEmpty(SteviaContext.getParam("readTimeout")) ? "10" : SteviaContext.getParam("readTimeout");

        ClientConfig baseConfig = ClientConfig.defaultConfig().baseUrl(new URL(rcUrl)).readTimeout(Duration.ofMinutes(Integer.parseInt(readTimeout)));
        ClientConfig config = (SteviaContext.getParam("grid").equals("gridlastic") || !gridInfo.hasBasicAuth) ? baseConfig : baseConfig.authenticateAs(new UsernameAndPassword(gridInfo.userName, gridInfo.password));
        CommandExecutor executor = new HttpCommandExecutor(config);
        try {
            driver = new RemoteWebDriver(executor, desiredCapabilities);
        } catch (SessionNotCreatedException e) {
            if (e.getMessage().contains("Response code 500")) {
                SteviaLogger.warn("Retry on getting remoteWebDriver");
                driver = new RemoteWebDriver(executor, desiredCapabilities);
            } else {
                SteviaLogger.error("Exception on getting remoteWebDriver: " + e.getMessage());
                throw e;
            }
        } catch (
                Exception e) {
            SteviaLogger.error("Exception on getting remoteWebDriver: " + e.getMessage());
            throw e;
        }
        return driver;
    }

    private WebDriver getLocalDriver(Capabilities capabilities) {
        WebDriver driver;
        String browser = capabilities.getBrowserName();
        switch (browser) {
            case "chrome":
                driver = new ChromeDriver((ChromeOptions) capabilities);
                break;
            case "firefox":
                driver = new FirefoxDriver((FirefoxOptions) capabilities);
                break;
            case "MicrosoftEdge":
                driver = new EdgeDriver((EdgeOptions) capabilities);
                break;
            default:
                throw new IllegalStateException("Browser requested is invalid");
        }
        return driver;
    }

    /**
     * Set up the correct options in case of Gridlastic
     *
     * @param capabilities
     * @param wdHost
     */
    private GridInfo setGridlasticOptions(Capabilities capabilities, String wdHost) {
        GridInfo gridInfo = GridInfo.parseUrl(wdHost, "gridlastic");
        LinkedHashMap<String, String> credentialsMap = new LinkedHashMap<>();
        credentialsMap.put("gridlasticUser", gridInfo.userName);
        credentialsMap.put("gridlasticKey", gridInfo.password);
        ((LinkedHashMap<String, String>) capabilities.getCapability("gridlastic:options")).putAll(credentialsMap);
        return gridInfo;
    }

    /**
     * Extract grid parameters from grid url
     */
    private static class GridInfo {
        private final String userName;
        private final String password;
        private final String gridUrl;

        private final boolean hasBasicAuth;

        private GridInfo(String userName, String password, String gridUrl, boolean hasBasicAuth) {
            this.userName = userName;
            this.password = password;
            this.gridUrl = gridUrl;
            this.hasBasicAuth = hasBasicAuth;
        }

        static GridInfo parseUrl(String gridUrl, String grid) {
            boolean isBasicAuth = hasBasicAuth(gridUrl);
            String uName = isBasicAuth ? gridUrl.split("//")[1].split(":")[0] : null;
            String pwd = isBasicAuth ? gridUrl.split("//")[1].split(":")[1].split("@")[0] : null;
            String url = (grid.equals("gridlastic") || !isBasicAuth) ? gridUrl : gridUrl.replace(uName + ':' + pwd + '@', "");
            return new GridInfo(uName, pwd, url, isBasicAuth);
        }

        private static boolean hasBasicAuth(String gridUrl) {
            return gridUrl.matches("(http|https)://\\w+:\\w+@.*");
        }
    }
}
