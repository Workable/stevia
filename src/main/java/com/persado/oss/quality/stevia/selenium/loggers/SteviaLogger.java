package com.persado.oss.quality.stevia.selenium.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;

public class SteviaLogger extends Reporter {

    static Logger LOG = LoggerFactory.getLogger("SteviaLogger");

    static String PASS_COLOR = "lime";

    static String WARN_COLOR = "orange";

    static String ERROR_COLOR = "red";

    /**
     * Info message
     *
     * @param message the message
     */
    public static void info(String message) {
        LOG.info(message);
        log("<div class=\"testOutput\" style=\"font-size:1em\">" + message + "</div>");
    }

    /**
     * Warning message
     *
     * @param message the message
     */
    public static void warn(String message) {
        LOG.warn(message);
        log("<div class=\"testOutput\" style=\"color:" + WARN_COLOR + "; font-size:1em;\">" + message + "</div>");
    }

    /**
     * Error message
     *
     * @param message the message
     */
    public static void error(String message) {
        LOG.error(message);
        log("<div class=\"testOutput\" style=\"color:" + ERROR_COLOR + "; font-size:1em;\">" + message + "</div>");
    }

    /**
     * Pass test message
     *
     * @param message
     */
    public static void pass(String message) {
        LOG.info(message);
        log("<div class=\"testOutput\" style=\"color:" + PASS_COLOR + "; font-size:1em;\">" + message + "</div>");
    }
}