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
import org.apache.flink.runtime.concurrent.ScheduledExecutor;
import org.apache.flink.runtime.concurrent.ScheduledExecutorServiceAdapter;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.netty.NettyClient;
import org.openqa.selenium.remote.tracing.TracedHttpClient;
import org.openqa.selenium.remote.tracing.Tracer;
import org.openqa.selenium.remote.tracing.opentelemetry.OpenTelemetryTracer;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
        if (SteviaContext.getParam("remote").compareTo(SteviaWebControllerFactory.TRUE) == 0) {
            final String wdHost = SteviaContext.getParam("rcUrl");
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            ScheduledExecutor executor = new ScheduledExecutorServiceAdapter(executorService);
            driver = getRemoteWebDriver(wdHost, capabilities);
            executorService.shutdown();
            ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        } else {
            driver = getLocalDriver(capabilities);
        }

        //Navigate to the desired target host url
        if (SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL) != null) {
            driver.get(SteviaContext.getParam(SteviaWebControllerFactory.TARGET_HOST_URL));
        }
        wdController.setDriver(driver);
        return wdController;
    }

    @Override
    public String getBeanName() {
        return "webDriverController";
    }

    private WebDriver getRemoteWebDriver(String rcUrl, Capabilities desiredCapabilities) throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        WebDriver driver = null;
        /**
         * When setting readTimeout RemoteWebDriver creation
         * we need to take into consideration the time needed for the Grid node to be spawned
         * Gridlastic suggests setting it to 10 minutes
         */
        ClientConfig config = ClientConfig.defaultConfig().baseUrl(new URL(rcUrl)).readTimeout(Duration.ofMinutes(Integer.parseInt(SteviaContext.getParam("nodeTimeout")))).withRetries();
        Tracer tracer = OpenTelemetryTracer.getInstance();
        CommandExecutor executor = new HttpCommandExecutor(Collections.emptyMap(), config, new TracedHttpClient.Factory(tracer, org.openqa.selenium.remote.http.HttpClient.Factory.createDefault()));
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
        setTimeout(driver);
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
            default:
                throw new IllegalStateException("Browser requested is invalid");
        }
        return driver;
    }

    /**
     * Set read timeout on NettyClient of WebDriver using Reflection
     * This is needed in order to update the readTimeout private field at a later time of instatiation of RemoteWebDriver object
     * ReadTimeout should have a reasonable value
     * @param driver
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setTimeout(WebDriver driver) throws NoSuchFieldException, IllegalAccessException {
        if (driver instanceof RemoteWebDriver) {
            Field clientOfExecutor = HttpCommandExecutor.class.getDeclaredField("client");
            Field delegatedClient = TracedHttpClient.class.getDeclaredField("delegate");
            Field config = NettyClient.class.getDeclaredField("config");
            Field readTimeout = ClientConfig.class.getDeclaredField("readTimeout");
            clientOfExecutor.setAccessible(true);
            delegatedClient.setAccessible(true);
            config.setAccessible(true);
            readTimeout.setAccessible(true);
            HttpCommandExecutor executor = (HttpCommandExecutor) ((RemoteWebDriver) driver).getCommandExecutor();
            TracedHttpClient tracedClient = (TracedHttpClient) clientOfExecutor.get(executor);
            NettyClient client = (NettyClient) delegatedClient.get(tracedClient);
            ClientConfig finalConfig = (ClientConfig) config.get(client);
            readTimeout.set(finalConfig, Duration.ofSeconds(60));
        }
    }
}
