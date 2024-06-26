package com.persado.oss.quality.stevia.selenium.core;

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

import com.persado.oss.quality.stevia.annotations.AnnotationsHelper;
import com.persado.oss.quality.stevia.selenium.core.controllers.WebControllerBase;
import com.persado.oss.quality.stevia.selenium.core.controllers.WebDriverWebController;
import com.persado.oss.quality.stevia.testng.Verify;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The Class SteviaContext.
 */
public class SteviaContext {

    /**
     * The Constant STEVIA_CONTEXT_LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SteviaContext.class);

    private static final AtomicInteger threadSeq = new AtomicInteger((int) (System.currentTimeMillis() % 0xcafe));

    /**
     * The inner Class Context.
     */
    static class Context {

        /**
         * The controller.
         */
        private WebController controller;

        /**
         * The verify.
         */
        private Verify verify;

        /**
         * The is web driver.
         */
        private boolean isWebDriver;

        /**
         * The params registry.
         */
        private Map<String, String> paramsRegistry;

        /**
         * The desired capabilities
         */
        private Capabilities capabilities;

        private int waitForPageToLoad = 30;
        private int waitForAjaxComplete = 20000;
        private int waitForElement = 10;
        private int waitForWindow = 10;
        private int waitForElementInvisibility = 1;

        private ApplicationContext context;

        private TestState state;

        private String testClassName;
        private String testMethodName;

        /**
         * Clear context.
         */
        public void clear() {
            AnnotationsHelper.disposeControllers();
            if (controller != null) {
                try {
                    controller.quit();
                } catch (WebDriverException wde) {
                    LOG.warn("Exception caught calling controller.quit(): \"" + wde.getMessage() + "\" additional info: " + wde.getAdditionalInformation());
                }
            }
            controller = null;
            verify = null;
            isWebDriver = false;
            if (paramsRegistry != null) {
                paramsRegistry.clear();
            }
            capabilities = null;
            context = null;
            state = null;
            LOG.info("Context closed, controller shutdown");
        }

        public int getWaitForPageToLoad() {
            return waitForPageToLoad;
        }

        public void setWaitForPageToLoad(int waitForPageToLoad) {
            this.waitForPageToLoad = waitForPageToLoad;
        }

        public int getWaitForElement() {
            return waitForElement;
        }

        public void setWaitForElement(int waitForElement) {
            this.waitForElement = waitForElement;
        }

        public int getWaitForAjaxComplete() {
            return waitForAjaxComplete;
        }

        public void setWaitForAjaxComplete(int waitForAjaxComplete) {
            this.waitForAjaxComplete = waitForAjaxComplete;
        }

        public int getWaitForElementInvisibility() {
            return waitForElementInvisibility;
        }

        public void setWaitForElementInvisibility(int waitForElementInvisibility) {
            this.waitForElementInvisibility = waitForElementInvisibility;
        }

        public String getTargetHostUrl() {
            return getParam("targetHostUrl");
        }

        public String getTargetHostUrlDomain() {
            return getParam("targetHostUrl").split("\\.+")[1];
        }

        public int getWaitForWindow() {
            return waitForWindow;
        }

        public void setWaitForWindow(int waitForWindow) {
            this.waitForWindow = waitForWindow;
        }

        public ApplicationContext getContext() {
            return context;
        }

        public void setContext(ApplicationContext context) {
            this.context = context;
        }

        public String getTestClassName() {
            return testClassName;
        }

        public void setTestClassName(String testClassName) {
            this.testClassName = testClassName;
        }

        public String getTestMethodName() {
            return testMethodName;
        }

        public void setTestMethodName(String testMethodName) {
            this.testMethodName = testMethodName;
        }
    }


    /**
     * The inner context as a thread local variable.
     */
    private static ThreadLocal<Context> innerContext = new ThreadLocal<SteviaContext.Context>() {

        @Override
        protected Context initialValue() {
            return new Context(); //initial is empty;
        }

    };


    /**
     * Gets the web controller.
     *
     * @return the web controller
     */
    public static WebController getWebController() {
        return innerContext.get().controller;
    }

    public static <T extends WebControllerBase> WebController getWebController(Class<T> classType) {
        return classType.cast(getWebController());
    }

    public static <T extends WebControllerBase> WebDriver getWebDriver(Class<T> classType) {
        return getWebController(classType).getDriver();
    }

    /**
     * Determines the instance of the Web Controller
     *
     * @return true, if it is instance of WebDriverWebController false if it is instance of SeleniumWebController
     */
    public static boolean isWebDriver() {
        return innerContext.get().isWebDriver;
    }

    /**
     * Adds parameters to registry; if a parameter exists already it will be overwritten.
     *
     * @param params a type of SteviaContextParameters
     */
    public static void registerParameters(SteviaContextParameters params) {
        Map<String, String> paramsRegistry = innerContext.get().paramsRegistry;
        if (paramsRegistry == null) {
            innerContext.get().paramsRegistry = new HashMap<String, String>();
        }
        innerContext.get().paramsRegistry.putAll(params.getAllParameters());
        LOG.warn("Thread {} just registered", new Object[]{Thread.currentThread().getName()});
    }

    public static void registerCapabilities(Capabilities caps) {
        innerContext.get().capabilities = caps;
        LOG.warn("Thread {} just registered", new Object[]{Thread.currentThread().getName()});
    }

    /**
     * get a parameter from the registry.
     *
     * @param paramName the param name
     * @return the parameter value
     */
    public static String getParam(String paramName) {
        return innerContext.get().paramsRegistry.get(paramName);
    }


    public static Capabilities getCapabilities() {
        return innerContext.get().capabilities;
    }

    /**
     * Gets the params.
     *
     * @return a Map of the registered parameters
     */
    public static Map<String, String> getParams() {
        return innerContext.get().paramsRegistry;
    }

    /**
     * get the test state that is relevant to the running thread for this test script
     *
     * @return <T extends TestState> T an object that implements TestState
     */
    @SuppressWarnings("unchecked")
    public static <T extends TestState> T getTestState() {
        return (T) innerContext.get().state;
    }

    /**
     * set the test state at any given time for the running thread.
     *
     * @param state an object implementing the marker interface
     */
    public static <T extends TestState> void setTestState(T state) {
        innerContext.get().state = state;
    }


    /**
     * Register the controller in the context of current thread's copy for this thread-local variable.
     *
     * @param instance the new web controller
     */
    public static void setWebController(WebController instance) {
        Context context = innerContext.get();
        context.controller = instance;
        if (instance instanceof WebDriverWebController) {
            context.isWebDriver = true;
            LOG.warn("Handle is : " + ((WebDriverWebController) instance).getDriver().getWindowHandle());
        } else {
            context.isWebDriver = false;
        }

        Thread.currentThread().setName("Stevia [" + (context.isWebDriver ? "WD" : "RC") + " " + instance.getClass().getSimpleName() + "@" + Integer.toHexString(threadSeq.incrementAndGet()) + "]");
        LOG.info("Context ready, controller is now set, type is {}", context.isWebDriver ? "WebDriver" : "SeleniumRC");

    }

    /**
     * return a verify helper initialized with the right controller.
     *
     * @return the verify
     */
    public static Verify verify() {
        Context context = innerContext.get();
        if (context.verify == null) {
            context.verify = new Verify();
        }
        return context.verify;
    }


    /**
     * Clean the local thread context
     */
    public static void clean() {
        innerContext.get().clear();
        innerContext.remove();
    }


    public static int getWaitForPageToLoad() {
        return innerContext.get().getWaitForPageToLoad();
    }

    public static void setWaitForPageToLoad(int waitForPageToLoad) {
        innerContext.get().setWaitForPageToLoad(waitForPageToLoad);
    }

    public static int getWaitForElement() {
        return innerContext.get().getWaitForElement();
    }

    public static int getWaitForAjaxComplete() {
        return innerContext.get().getWaitForAjaxComplete();
    }

    public static void setWaitForAjaxComplete(int waitForAjaxComplete) {
        innerContext.get().setWaitForAjaxComplete(waitForAjaxComplete);
    }

    public static void setWaitForElement(int waitForElement) {
        innerContext.get().setWaitForElement(waitForElement);
    }

    public static int getWaitForElementInvisibility() {
        return innerContext.get().getWaitForElementInvisibility();
    }

    public static void setWaitForElementInvisibility(int waitForElementInvisibility) {
        innerContext.get().setWaitForElementInvisibility(waitForElementInvisibility);
    }

    public static void setWaitForNewWindow(int waitForNewWindow) {
        innerContext.get().setWaitForWindow(waitForNewWindow);
    }

    public static String getTargetHostUrl() {
        return innerContext.get().getTargetHostUrl();
    }

    public static String getTargetHostUrlDomain() {
        return innerContext.get().getTargetHostUrlDomain();
    }

    public static int getWaitForNewWindow() {
        return innerContext.get().getWaitForWindow();
    }

    public static void attachSpringContext(ApplicationContext applicationContext) {
        innerContext.get().setContext(applicationContext);
    }

    public static ApplicationContext getSpringContext() {
        return innerContext.get().getContext();
    }

    public static String getTestClassName() {
        return innerContext.get().getTestClassName();
    }

    public static void setTestClassName(String testClassName) {
        innerContext.get().setTestClassName(testClassName);
    }

    public static String getTestMethodName() {
        return innerContext.get().getTestMethodName();
    }

    public static void setTestMethodName(String testMethodName) {
        innerContext.get().setTestMethodName(testMethodName);
    }

}
