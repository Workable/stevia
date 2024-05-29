package com.persado.oss.quality.stevia.spring;

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

import com.persado.oss.quality.stevia.selenium.core.Constants;
import com.persado.oss.quality.stevia.selenium.core.SteviaContext;
import com.persado.oss.quality.stevia.selenium.core.SteviaContextSupport;
import com.persado.oss.quality.stevia.selenium.core.WebController;
import com.persado.oss.quality.stevia.selenium.core.controllers.SteviaWebControllerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.xml.XmlSuite;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The base class that is responsible for initializing Stevia contexts on start and shutting down on
 * test ends. It is parallel-aware and has options to start RC server locally if needed via XML
 * configuration parameters.
 */
@ContextConfiguration(locations = {"classpath:META-INF/spring/stevia-boot-context.xml"})
public class SteviaTestBase extends AbstractTestNGSpringContextTests implements Constants {

    /**
     * The Constant STEVIA_TEST_BASE_LOG.
     */
    private static final Logger STEVIA_TEST_BASE_LOG = LoggerFactory.getLogger(SteviaTestBase.class);

    /**
     * The selenium server.
     */
    private static Object[] seleniumServer;

    /**
     * Determined if RC server is started programmatically.
     */
    private static boolean isRCStarted = false;

    /**
     * suite-global output directory.
     */
    private static String suiteOutputDir;


    /**
     * Extends the TestNG method to prepare the Spring contexts for parallel tests.
     *
     * @throws Exception the exception
     */
    @BeforeSuite(alwaysRun = true)
    @BeforeClass(alwaysRun = true)
    @BeforeTest(alwaysRun = true)
    @Override
    protected final void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();
    }


    /**
     *
     * @throws Exception the exception
     */
    @BeforeSuite(alwaysRun = true)
    protected final void configureSuiteSettings() throws Exception {
        Map<String, String> parameters = configureParameters();

        //stevia context init

        STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");
        STEVIA_TEST_BASE_LOG.warn("*** SUITE initialisation phase START                                              ***");
        STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");

        // user code
        suiteInitialisation();

        SteviaContext.registerParameters(SteviaContextSupport.getParameters(parameters));
        STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");
        STEVIA_TEST_BASE_LOG.warn("*** SUITE initialisation phase END                                                ***");
        STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");
    }

    private static Map<String, String> configureParameters(ITestContext testContext) {
        Set<String> propNames = System.getProperties().stringPropertyNames();
        Map<String, String> parameters = new HashMap<>(testContext.getSuite().getXmlSuite().getAllParameters());
        propNames.forEach(p -> parameters.put(p, System.getProperty(p)));
        return parameters;
    }
    private static Map<String, String> configureParameters() {
        Map<String, String> parameters = new HashMap<>();
        Set<String> propNames = System.getProperties().stringPropertyNames();
        propNames.forEach(p -> parameters.put(p, System.getProperty(p)));
        return parameters;
    }

    /**
     * Suite-Level initialisation callback; this method should be overrriden to
     * allow suite-level configuration to happen - preferrably at the Base class
     * of the tests (overriden versions of this method will be called from the
     * class extending this Base, at Suite initialisation, best place for this
     * method to be overriden is at the class extending this Base class).
     *
     */
    protected void suiteInitialisation() {
        STEVIA_TEST_BASE_LOG.warn("***************************************************************************************");
        STEVIA_TEST_BASE_LOG.warn("*** suiteInitialisation() not overriden. Check your code and javadoc of method      ***");
        STEVIA_TEST_BASE_LOG.warn("*** NOTE: suiteInitialisation() by default has a SteviaContext to work with.        ***");
        STEVIA_TEST_BASE_LOG.warn("***************************************************************************************");
    }


    /**
     * Test-Level initialisation callback; this method should be overrriden to
     * allow suite-level configuration to happen - preferrably at the Base class
     * of the tests (overriden versions of this method will be called from the
     * class extending this Base, at Suite initialisation, best place for this
     * method to be overriden is at the class extending this Base class).
     *
     * @param context test context
     */
    protected void testInitialisation(ITestContext context) {
        STEVIA_TEST_BASE_LOG.warn("***************************************************************************************");
        STEVIA_TEST_BASE_LOG.warn("*** suiteInitialisation() not overriden. Check your code and javadoc of method      ***");
        STEVIA_TEST_BASE_LOG.warn("*** NOTE: suiteInitialisation() by default has a SteviaContext to work with.        ***");
        STEVIA_TEST_BASE_LOG.warn("***************************************************************************************");
    }

    /**
     * Before test.
     *
     * @param testContext the test context
     * @throws Exception the exception
     */
    @BeforeTest(alwaysRun = true)
    protected final void contextInitBeforeTest(ITestContext testContext) throws Exception {
        configureParameters(testContext);
        testInitialisation(testContext);
        Map<String, String> parameters = testContext.getCurrentXmlTest().getAllParameters();
        testContext.getCurrentXmlTest().setParallel(XmlSuite.ParallelMode.getValidParallel(parameters.get("parallelSetup")));
        String parallelSetup = testContext.getSuite().getParallel();

        if (parallelSetup == null || parallelSetup.isEmpty() || parallelSetup.equalsIgnoreCase("false") || parallelSetup.equalsIgnoreCase("none") || parallelSetup.equalsIgnoreCase("tests")) {

            STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");
            STEVIA_TEST_BASE_LOG.warn("*** Driver initialisation phase, current parallel level is @BeforeTest            ***");
            STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");
            initializeStevia(parameters);
        }
    }

    /**
     * Before class.
     *
     * @param testContext the test context
     * @throws Exception the exception
     */
    @BeforeClass(alwaysRun = true)
    protected final void contextInitBeforeClass(ITestContext testContext) throws Exception {

        Map<String, String> parameters = testContext.getSuite().getXmlSuite().getAllParameters();

        if (testContext.getSuite().getParallel().equalsIgnoreCase("classes")) {

            STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");
            STEVIA_TEST_BASE_LOG.warn("*** Driver initialisation phase, current parallel level is @BeforeClass**************");
            STEVIA_TEST_BASE_LOG.warn("*************************************************************************************");

            initializeStevia(parameters);
        }

        // Set testClassName
        SteviaContext.setTestClassName(getClass().getSimpleName());
    }

    /**
     * Before method.
     *
     * @param testContext the test context
     * @throws Exception the exception
     */
    @BeforeMethod(alwaysRun = true)
    protected final void contextInitBeforeMethod(ITestContext testContext, Method method) throws Exception {
        Map<String, String> parameters = testContext.getSuite().getXmlSuite().getAllParameters();

        if (testContext.getSuite().getParallel().equalsIgnoreCase("methods")) {

            STEVIA_TEST_BASE_LOG.warn("***************************************************************************************");
            STEVIA_TEST_BASE_LOG.warn("*** Driver initialisation phase, current parallel level is @BeforeMethod[PANICMODE] ***");
            STEVIA_TEST_BASE_LOG.warn("***************************************************************************************");

            initializeStevia(parameters);
        }

        // Set testMethodName
        SteviaContext.setTestMethodName(method.getName());
    }

    /**
     * Clean context on class.
     *
     * @param testContext the test context
     */
    @AfterClass(alwaysRun = true)
    protected final void cleanContextOnClass(ITestContext testContext) {
        if (testContext.getSuite().getParallel().equalsIgnoreCase("classes")) {
            SteviaContext.clean();
        }
    }

    /**
     * Clean context on test.
     *
     * @param testContext the test context
     */
    @AfterTest(alwaysRun = true)
    protected final void cleanContextOnTest(ITestContext testContext) {
        String parallelSetup = testContext.getSuite().getParallel();
        if (parallelSetup == null || parallelSetup.isEmpty()
                || parallelSetup.equalsIgnoreCase("false")
                || parallelSetup.equalsIgnoreCase("tests")) {
            SteviaContext.clean();
            if (testContext.getCurrentXmlTest().getParameter("useNewWDA") != null && testContext.getCurrentXmlTest().getParameter("useNewWDA").equals("true")) {
                STEVIA_TEST_BASE_LOG.info("Uninstall WDA");
                uninstallApp(testContext.getCurrentXmlTest().getParameter("mobileDeviceUUID"), "com.apple.test.WebDriverAgentRunner-Runner");
            }
        }
    }

    /**
     * Clean context on method.
     *
     * @param testContext the test context
     */
    @AfterMethod(alwaysRun = true)
    protected final void cleanContextOnMethod(ITestContext testContext) {
        if (testContext.getSuite().getParallel().equalsIgnoreCase("methods")) {
            SteviaContext.clean();
        }
    }


    /**
     * Initialize driver.
     *
     * @param params
     * @throws Exception
     */
    protected final void initializeStevia(Map<String, String> params) throws Exception {
        if (applicationContext == null) {
            super.springTestContextPrepareTestInstance();
        }

        SteviaContext.registerParameters(SteviaContextSupport.getParameters(params));
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not set - Stevia cannot continue");
        }
        SteviaContext.attachSpringContext(applicationContext);

        if (params.get("run.api.test") == null || params.get("run.api.test").equalsIgnoreCase("false")) {
            configureWebController();
        }

    }

    private void configureWebController() throws MalformedURLException, InterruptedException, ExecutionException, TimeoutException, NoSuchFieldException, IllegalAccessException {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not set - Stevia cannot continue");
        }

        WebController controller = SteviaWebControllerFactory.getWebController(applicationContext);
        SteviaContext.setWebController(controller);
    }

    /**
     * Gets the suite output dir.
     *
     * @return the suite output dir
     */
    public static String getSuiteOutputDir() {
        return suiteOutputDir;
    }

    private void uninstallApp(String deviceId, String appId) {
        ProcessBuilder pb = new ProcessBuilder("ideviceinstaller", "-u", deviceId, "-U", appId);
        pb.redirectOutput();
        try {
            pb.start().waitFor(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            STEVIA_TEST_BASE_LOG.error(e.getMessage());
        }
    }


}
