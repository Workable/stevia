package com.persado.oss.quality.stevia.selenium.loggers;

/*
 * #%L
 * Stevia QA Framework - Core
 * %%
 * Copyright (C) 2013 - 2021 Persado Intellectual Property Limited
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